package com.rattatarr.rattatarr.services.schedulers;

import com.rattatarr.rattatarr.clients.jellyfin.JellyfinClient;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientActivityLogEntryResponseDTO;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientPlaybackItemResponseDTO;
import com.rattatarr.rattatarr.clients.jellyfin.responses.wrappers.JellyfinClientActivityLogEntriesWrapper;
import com.rattatarr.rattatarr.models.WatchEventType;
import com.rattatarr.rattatarr.models.entities.*;
import com.rattatarr.rattatarr.repositories.WatchEventsRepository;
import com.rattatarr.rattatarr.services.MediaEpisodesService;
import com.rattatarr.rattatarr.services.MediaItemsService;
import com.rattatarr.rattatarr.services.ProfilesService;
import com.rattatarr.rattatarr.services.SettingsService;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final List<WatchEvent> pendingWatchEvents = new ArrayList<>();

    private final JellyfinClient jellyfinClient;
    private final SettingsService settingsService;
    private final ProfilesService profilesService;
    private final MediaItemsService mediaItemsService;
    private final MediaEpisodesService mediaEpisodesService;
    private final WatchEventsRepository watchEventsRepository;

    public JellyfinActivityPollingService(
            JellyfinClient jellyfinClient,
            SettingsService settingsService,
            ProfilesService profilesService,
            MediaItemsService mediaItemsService,
            MediaEpisodesService mediaEpisodesService,
            WatchEventsRepository watchEventsRepository
    ) {
        this.jellyfinClient = jellyfinClient;
        this.settingsService = settingsService;
        this.profilesService = profilesService;
        this.mediaItemsService = mediaItemsService;
        this.mediaEpisodesService = mediaEpisodesService;
        this.watchEventsRepository = watchEventsRepository;
    }

    @Scheduled(
            fixedDelayString = "${rattatarr.sync.jellyfin-activity-poll-interval:PT10M}",
            initialDelayString = "${rattatarr.sync.jellyfin-activity-poll-initial-delay:PT10M}"
    )
    @Transactional
    public void pollJellyfinActivity() {
        boolean enabled = settingsService.getBooleanSetting(SettingsService.SYNC_JELLYFIN_ENABLED, true);
        if (!enabled) {
            logger.debug("Jellyfin activity polling is disabled via settings");
            return;
        }

        if (!jellyfinClient.isConfigured()) {
            logger.debug("Jellyfin activity polling skipped: Jellyfin not configured");
            return;
        }

        List<JellyfinClientActivityLogEntryResponseDTO> entries = loadEntriesForCurrentCycle();
        if (entries == null || entries.isEmpty()) {
            return;
        }

        Set<Long> polledEntryIds = entries.stream()
                .map(JellyfinClientActivityLogEntryResponseDTO::id)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        Set<Long> existingEntryIds = polledEntryIds.isEmpty()
                ? Set.of()
                : watchEventsRepository.findExistingJellyfinLogIds(polledEntryIds);

        entries.stream()
                .sorted(Comparator.comparing(JellyfinClientActivityLogEntryResponseDTO::id))
                .filter(this::isSupportedPlaybackEntry)
                .filter(entry -> entry.id() != null)
                .filter(entry -> !existingEntryIds.contains(entry.id()))
                .forEach(this::processEntry);

        if (!pendingWatchEvents.isEmpty()) {
            watchEventsRepository.saveAll(new ArrayList<>(pendingWatchEvents));
            pendingWatchEvents.clear();
        }
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

    private void processEntry(JellyfinClientActivityLogEntryResponseDTO entry) {
        try {
            if (entry.id() == null) {
                return;
            }

            Optional<Profile> profileOptional = profilesService.findByJellyfinId(entry.userId());
            if (profileOptional.isEmpty()) {
                return;
            }

            var resolution = resolveMediaTargets(entry.itemId());
            if (resolution.mediaItem().isEmpty()) {
                return;
            }

            JellyfinClientPlaybackItemResponseDTO playbackItem = fetchPlaybackItem(entry);
            WatchEventType eventType = resolveEventType(entry, playbackItem);
            Integer positionSeconds = resolvePositionSeconds(entry, playbackItem);

            WatchEvent watchEvent = new WatchEvent(
                    entry.id(),
                    profileOptional.get(),
                    resolution.mediaItem().get(),
                    resolution.mediaSeason().orElse(null),
                    resolution.episode().orElse(null),
                    eventType,
                    parseDateOrNow(entry.date()),
                    positionSeconds
            );
            pendingWatchEvents.add(watchEvent);
        } catch (Exception e) {
            logger.error("Failed processing Jellyfin activity entry id={} type={}", entry.id(), entry.type(), e);
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
