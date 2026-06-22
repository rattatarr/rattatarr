package com.rattatarr.rattatarr.services.schedulers;

import com.rattatarr.rattatarr.services.JellyfinTraversalService;
import com.rattatarr.rattatarr.services.MediaItemCreditsService;
import com.rattatarr.rattatarr.services.MediaItemMetadataService;
import com.rattatarr.rattatarr.services.SettingsService;
import com.rattatarr.rattatarr.utils.MdcContext;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
@NullMarked
public class MediaSyncScheduler {
    private static final Logger logger = LoggerFactory.getLogger(MediaSyncScheduler.class);

    private final JellyfinTraversalService jellyfinTraversalService;
    private final MediaItemMetadataService mediaItemMetadataService;
    private final MediaItemCreditsService mediaItemCreditsService;
    private final SettingsService settingsService;

    public MediaSyncScheduler(
            JellyfinTraversalService jellyfinTraversalService,
            MediaItemMetadataService mediaItemMetadataService,
            MediaItemCreditsService mediaItemCreditsService,
            SettingsService settingsService
    ) {
        this.jellyfinTraversalService = jellyfinTraversalService;
        this.mediaItemMetadataService = mediaItemMetadataService;
        this.mediaItemCreditsService = mediaItemCreditsService;
        this.settingsService = settingsService;
    }

    /**
     * Scheduled Jellyfin media synchronization.
     * Runs periodically to discover new seasons/episodes for existing series
     * and import any new movies/series added to Jellyfin.
     * <p>
     * Default: Runs every 24 hours with 5-minute initial delay.
     * Configurable via application.yaml properties.
     */
    @Scheduled(
            fixedDelayString = "${rattatarr.sync.jellyfin-interval:PT24H}",
            initialDelayString = "${rattatarr.sync.jellyfin-initial-delay:PT5M}"
    )
    public void scheduledJellyfinSync() {
        boolean enabled = settingsService.getBooleanSetting(
                SettingsService.SYNC_JELLYFIN_ENABLED,
                true
        );

        if (!enabled) {
            logger.debug("Jellyfin scheduled sync is disabled via settings");
            return;
        }

        try (var ignored = MdcContext.of(Map.of("jobType", "JELLYFIN_SYNC", "jobSource", "SCHEDULER"))) {
            Instant startTime = Instant.now();
            logger.info("Starting scheduled Jellyfin media synchronization");

            try {
                jellyfinTraversalService.pipelineTraverseSyncMedia();
                mediaItemMetadataService.refreshAllMetadata(false);
                mediaItemCreditsService.updateAllMediaItemCredits(false);

                Duration duration = Duration.between(startTime, Instant.now());
                logger.info("Scheduled Jellyfin sync completed successfully in {} seconds",
                        duration.getSeconds());
            } catch (Exception e) {
                logger.error("Scheduled Jellyfin sync failed", e);
            }
        }
    }
}
