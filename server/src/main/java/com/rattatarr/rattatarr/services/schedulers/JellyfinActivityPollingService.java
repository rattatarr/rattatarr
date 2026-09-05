package com.rattatarr.rattatarr.services.schedulers;

import com.rattatarr.rattatarr.clients.jellyfin.JellyfinClient;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientActivityLogEntryResponseDTO;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientPlaybackItemResponseDTO;
import com.rattatarr.rattatarr.clients.jellyfin.responses.wrappers.JellyfinClientActivityLogEntriesWrapper;
import com.rattatarr.rattatarr.models.WatchEventType;
import com.rattatarr.rattatarr.models.entities.*;
import com.rattatarr.rattatarr.repositories.WatchEventsRepository;
import com.rattatarr.rattatarr.services.*;
import com.rattatarr.rattatarr.utils.MdcContext;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@NullMarked
public class JellyfinActivityPollingService {
    private static final Logger logger = LoggerFactory.getLogger(JellyfinActivityPollingService.class);
    private static final long TICKS_PER_SECOND = 10_000_000L;
    private static final String VIDEO_PLAYBACK = "VideoPlayback";
    private static final String VIDEO_PLAYBACK_STOPPED = "VideoPlaybackStopped";

    private final JellyfinClient jellyfinClient;
    private final SettingsService settingsService;
    private final ProfilesService profilesService;
    private final MediaItemsService mediaItemsService;
    private final MediaEpisodesService mediaEpisodesService;
    private final WatchEventsRepository watchEventsRepository;
    private final BackgroundJobService backgroundJobService;

    public JellyfinActivityPollingService(
            JellyfinClient jellyfinClient,
            SettingsService settingsService,
            ProfilesService profilesService,
            MediaItemsService mediaItemsService,
            MediaEpisodesService mediaEpisodesService,
            WatchEventsRepository watchEventsRepository,
            BackgroundJobService backgroundJobService
    ) {
        this.jellyfinClient = jellyfinClient;
        this.settingsService = settingsService;
        this.profilesService = profilesService;
        this.mediaItemsService = mediaItemsService;
        this.mediaEpisodesService = mediaEpisodesService;
        this.watchEventsRepository = watchEventsRepository;
        this.backgroundJobService = backgroundJobService;
    }

    @Async("backgroundTaskExecutor")
    public void triggerPollAsync(BackgroundJob job) {
        try (var ignored = MdcContext.of(Map.of("jobId", job.id().toString(), "jobType", job.type().name()))) {
            try {
                // Add a small delaying because this might be too fast and the client side will not be able to connect
                // to the websocket properly and will be locked in an infinite loading
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try {
                backgroundJobService.markRunning(job);
                doPollJellyfinActivity();
                backgroundJobService.markCompleted(job, "Activity poll completed successfully");
            } catch (Exception e) {
                logger.error("Manual Jellyfin activity poll failed", e);
                backgroundJobService.markFailed(job, e.getMessage());
            }
        }
    }

    @Scheduled(
            fixedDelayString = "${rattatarr.sync.jellyfin-activity-poll-interval:PT10M}",
            initialDelayString = "${rattatarr.sync.jellyfin-activity-poll-initial-delay:PT10M}"
    )
    @Transactional
    public void pollJellyfinActivity() {
        try (var ignored = MdcContext.of(Map.of("jobType", "JELLYFIN_ACTIVITY_POLL"))) {
            doPollJellyfinActivity();
        }
    }

    private void doPollJellyfinActivity() {
        boolean enabled = settingsService.getBooleanSetting(SettingsService.SYNC_JELLYFIN_ENABLED, true);
        if (!enabled) {
            logger.debug("Jellyfin activity polling is disabled via settings");
            return;
        }

        if (!jellyfinClient.isConfigured()) {
            logger.debug("Jellyfin activity polling skipped: Jellyfin not configured");
            return;
        }

        processActivityLog();
        processManualPlayedMarks();
    }

    private void processActivityLog() {
        List<JellyfinClientActivityLogEntryResponseDTO> entries = loadEntriesForCurrentCycle();
        if (entries == null || entries.isEmpty()) {
            logger.debug("Jellyfin activity poll: no activity entries returned");
            return;
        }
        logger.debug("Jellyfin activity poll: scanning {} activity entries", entries.size());

        Set<Long> polledEntryIds = entries.stream()
                .map(JellyfinClientActivityLogEntryResponseDTO::id)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        Set<Long> existingEntryIds = polledEntryIds.isEmpty()
                ? Set.of()
                : watchEventsRepository.findExistingJellyfinLogIds(polledEntryIds);

        List<WatchEvent> pendingWatchEvents = entries.stream()
                .sorted(Comparator.comparing(JellyfinClientActivityLogEntryResponseDTO::id))
                .filter(this::isSupportedPlaybackEntry)
                .filter(entry -> entry.id() != null)
                .filter(entry -> !existingEntryIds.contains(entry.id()))
                .map(this::buildPlaybackWatchEvent)
                .flatMap(Optional::stream)
                .toList();

        if (!pendingWatchEvents.isEmpty()) {
            watchEventsRepository.saveAll(pendingWatchEvents);
            logger.info("Jellyfin activity poll: persisted {} new watch event(s) from {} scanned entries",
                    pendingWatchEvents.size(), entries.size());
        } else {
            logger.debug("Jellyfin activity poll: no new watch events from {} scanned entries", entries.size());
        }
    }

    private void processManualPlayedMarks() {
        List<WatchEvent> pendingWatchEvents = profilesService.findAllWithJellyfinId().stream()
                .flatMap(profile -> collectManualPlayedMarks(profile).stream())
                .toList();

        if (!pendingWatchEvents.isEmpty()) {
            watchEventsRepository.saveAll(pendingWatchEvents);
            logger.info("Jellyfin manual-played scan: persisted {} new COMPLETE watch event(s)",
                    pendingWatchEvents.size());
        } else {
            logger.debug("Jellyfin manual-played scan: no new COMPLETE watch events");
        }
    }

    private List<WatchEvent> collectManualPlayedMarks(Profile profile) {
        List<JellyfinClientPlaybackItemResponseDTO> playedItems;
        try {
            playedItems = jellyfinClient.getPlayedItemsForUser(profile.jellyfinId()).items();
        } catch (Exception e) {
            logger.error("Jellyfin manual-played scan: failed to load played items for user {}",
                    profile.jellyfinId(), e);
            return List.of();
        }
        if (playedItems == null || playedItems.isEmpty()) {
            return List.of();
        }

        return playedItems.stream()
                .map(item -> buildManualPlayedMark(profile, item))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<WatchEvent> buildManualPlayedMark(Profile profile, JellyfinClientPlaybackItemResponseDTO item) {
        try {
            if (item.id() == null) {
                return Optional.empty();
            }
            var resolution = resolveMediaTargets(item.id());
            if (resolution.mediaItem().isEmpty()) {
                logger.warn(
                        "Jellyfin manual-played scan: no local media matches Jellyfin item id={} type={} name='{}' "
                                + "(no MediaItem or MediaEpisode carries this jellyfinId) - run a full Jellyfin sync to back-fill",
                        item.id(), item.type(), item.name());
                return Optional.empty();
            }
            if (alreadyHasCompleteEvent(profile, resolution)) {
                return Optional.empty();
            }

            return Optional.of(new WatchEvent(
                    null,
                    profile,
                    resolution.mediaItem().get(),
                    resolution.mediaSeason().orElse(null),
                    resolution.episode().orElse(null),
                    WatchEventType.COMPLETE,
                    Instant.now(),
                    null
            ));
        } catch (Exception e) {
            logger.error("Jellyfin manual-played scan: failed processing played item id={}", item.id(), e);
            return Optional.empty();
        }
    }

    private boolean alreadyHasCompleteEvent(Profile profile, MediaResolution resolution) {
        if (resolution.episode().isPresent()) {
            return watchEventsRepository.existsByProfile_IdAndEpisode_IdAndEventType(
                    profile.id(), resolution.episode().get().id(), WatchEventType.COMPLETE);
        }
        return watchEventsRepository.existsByProfile_IdAndMediaItem_IdAndEpisodeIsNullAndEventType(
                profile.id(), resolution.mediaItem().get().id(), WatchEventType.COMPLETE);
    }

    private List<JellyfinClientActivityLogEntryResponseDTO> loadEntriesForCurrentCycle() {
        boolean backfillComplete = settingsService.getBooleanSetting(
                SettingsService.SYNC_JELLYFIN_ACTIVITY_BACKFILL_COMPLETE,
                false
        );
        if (backfillComplete) {
            JellyfinClientActivityLogEntriesWrapper wrapper = jellyfinClient.getActivityLogEntries();
            return wrapper.items() == null ? List.of() : wrapper.items();
        }

        List<JellyfinClientActivityLogEntryResponseDTO> collected = new ArrayList<>();
        int startIndex = 0;
        while (true) {
            JellyfinClientActivityLogEntriesWrapper wrapper = jellyfinClient.getActivityLogEntries(startIndex);
            List<JellyfinClientActivityLogEntryResponseDTO> batch = wrapper.items();
            if (batch == null || batch.isEmpty()) {
                break;
            }

            collected.addAll(batch);
            Integer totalCount = wrapper.totalRecordCount();
            if (totalCount != null && (startIndex + batch.size()) >= totalCount) {
                break;
            }
            startIndex += 50;
        }

        settingsService.updateSetting(
                SettingsService.SYNC_JELLYFIN_ACTIVITY_BACKFILL_COMPLETE,
                "true",
                null
        );

        return collected;
    }

    private boolean isSupportedPlaybackEntry(JellyfinClientActivityLogEntryResponseDTO entry) {
        return VIDEO_PLAYBACK.equals(entry.type()) || VIDEO_PLAYBACK_STOPPED.equals(entry.type());
    }

    private Optional<WatchEvent> buildPlaybackWatchEvent(JellyfinClientActivityLogEntryResponseDTO entry) {
        try {
            if (entry.id() == null) {
                return Optional.empty();
            }

            Optional<Profile> profileOptional = profilesService.findByJellyfinId(entry.userId());
            if (profileOptional.isEmpty()) {
                return Optional.empty();
            }

            var resolution = resolveMediaTargets(entry.itemId());
            if (resolution.mediaItem().isEmpty()) {
                logger.warn(
                        "Jellyfin activity poll: no local media matches Jellyfin item id={} (entry id={} type={} name='{}') "
                                + "- watch event NOT recorded; run a full Jellyfin sync to back-fill the missing jellyfinId",
                        entry.itemId(), entry.id(), entry.type(), entry.name());
                return Optional.empty();
            }

            JellyfinClientPlaybackItemResponseDTO playbackItem = fetchPlaybackItem(entry);
            WatchEventType eventType = resolveEventType(entry, playbackItem);
            Integer positionSeconds = resolvePositionSeconds(entry, playbackItem);

            return Optional.of(new WatchEvent(
                    entry.id(),
                    profileOptional.get(),
                    resolution.mediaItem().get(),
                    resolution.mediaSeason().orElse(null),
                    resolution.episode().orElse(null),
                    eventType,
                    parseDateOrNow(entry.date()),
                    positionSeconds
            ));
        } catch (Exception e) {
            logger.error("Failed processing Jellyfin activity entry id={} type={} itemId={}",
                    entry.id(), entry.type(), entry.itemId(), e);
            return Optional.empty();
        }
    }

    private WatchEventType resolveEventType(
            JellyfinClientActivityLogEntryResponseDTO entry,
            @Nullable JellyfinClientPlaybackItemResponseDTO playbackItem
    ) {
        if (VIDEO_PLAYBACK.equals(entry.type())) {
            return WatchEventType.START;
        }

        Boolean played = playbackItem != null && playbackItem.userData() != null
                ? playbackItem.userData().played()
                : null;
        if (Boolean.TRUE.equals(played)) {
            return WatchEventType.COMPLETE;
        }
        return WatchEventType.PROGRESS;
    }

    @Nullable
    private Integer resolvePositionSeconds(
            JellyfinClientActivityLogEntryResponseDTO entry,
            @Nullable JellyfinClientPlaybackItemResponseDTO playbackItem
    ) {
        if (VIDEO_PLAYBACK.equals(entry.type())) {
            return null;
        }

        if (playbackItem == null
                || playbackItem.userData() == null
                || playbackItem.userData().playbackPositionTicks() == null) {
            return null;
        }

        long seconds = playbackItem.userData().playbackPositionTicks() / TICKS_PER_SECOND;
        if (seconds > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) seconds;
    }

    @Nullable
    private JellyfinClientPlaybackItemResponseDTO fetchPlaybackItem(JellyfinClientActivityLogEntryResponseDTO entry) {
        if (!VIDEO_PLAYBACK_STOPPED.equals(entry.type())) {
            return null;
        }
        return jellyfinClient.getItemByIdForUser(entry.itemId(), entry.userId());
    }

    private Instant parseDateOrNow(@Nullable String date) {
        if (date == null || date.isBlank()) {
            return Instant.now();
        }
        try {
            return Instant.parse(date);
        } catch (DateTimeParseException e) {
            logger.debug("Unable to parse Jellyfin activity date '{}', using now", date);
            return Instant.now();
        }
    }

    private MediaResolution resolveMediaTargets(String itemId) {
        Optional<MediaItem> mediaItem = mediaItemsService.findByJellyfinId(itemId);
        if (mediaItem.isPresent()) {
            return new MediaResolution(mediaItem, Optional.empty(), Optional.empty());
        }

        Optional<MediaEpisode> episode = mediaEpisodesService.findByJellyfinId(itemId);
        return episode.map(mediaEpisode -> new MediaResolution(
                Optional.of(mediaEpisode.mediaSeason().mediaItem()),
                Optional.of(mediaEpisode.mediaSeason()),
                Optional.of(mediaEpisode)
        )).orElseGet(() -> new MediaResolution(Optional.empty(), Optional.empty(), Optional.empty()));

    }

    private record MediaResolution(
            Optional<MediaItem> mediaItem,
            Optional<MediaSeason> mediaSeason,
            Optional<MediaEpisode> episode
    ) {
    }
}
