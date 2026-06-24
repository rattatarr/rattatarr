package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.utils.MdcContext;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@NullMarked
public class MediaItemRefreshService {
    private static final Logger logger = LoggerFactory.getLogger(MediaItemRefreshService.class);

    private final MediaItemsService mediaItemsService;
    private final TMDbService tmDbService;
    private final JellyfinTraversalService jellyfinTraversalService;
    private final SettingsService settingsService;

    public MediaItemRefreshService(
            MediaItemsService mediaItemsService,
            TMDbService tmDbService,
            JellyfinTraversalService jellyfinTraversalService,
            SettingsService settingsService
    ) {
        this.mediaItemsService = mediaItemsService;
        this.tmDbService = tmDbService;
        this.jellyfinTraversalService = jellyfinTraversalService;
        this.settingsService = settingsService;
    }

    /**
     * Refreshes a media item if it's stale.
     * Routes to appropriate refresh service based on source (TMDb vs Jellyfin).
     * <p>
     * - TMDb series (jellyfinId == null): Refreshed if updatedAt > threshold
     * - Jellyfin series (jellyfinId != null): Refreshed if updatedAt > threshold
     *
     * @param mediaItem The media item to potentially refresh
     * @return Mono with refreshed or original media item
     */
    public MediaItem refreshIfStale(MediaItem mediaItem) {
        if (mediaItem.mediaType() != MediaType.SERIES) {
            return mediaItem;
        }

        Duration staleThreshold = settingsService.getDurationSetting(
                SettingsService.SYNC_STALE_THRESHOLD,
                Duration.ofDays(2)
        );

        if (!mediaItemsService.isStale(mediaItem, staleThreshold)) {
            return mediaItem;
        }

        String title = mediaItem.title() != null ? mediaItem.title() : "Untitled";
        String source = mediaItem.jellyfinId() != null ? "Jellyfin" : "TMDb";
        logger.info("Series '{}' (ID: {}) is stale (updated: {}), triggering refresh from {}",
                title, mediaItem.id(), mediaItem.updatedAt(), source);

        try {
            MediaItem refreshed = refresh(mediaItem);
            logger.info("Successfully refreshed series '{}' from {}", refreshed.title(), source);
            return refreshed;
        } catch (Exception e) {
            logger.error("Failed to refresh series '{}' from {}", title, source, e);
            return mediaItem; // Return original on error
        }
    }

    /**
     * Unified refresh method that routes to appropriate service based on source.
     * Supports both TMDb-imported and Jellyfin-sourced series.
     *
     * @param mediaItem The media item to refresh
     * @return Mono with refreshed media item
     */
    public MediaItem refresh(MediaItem mediaItem) {
        if (mediaItem.mediaType() != MediaType.SERIES) {
            throw new CommonExceptions.InvalidRequestExceptions(
                    "Can only refresh series, got: " + mediaItem.mediaType());
        }

        // Route based on source
        if (mediaItem.jellyfinId() != null) {
            logger.info("Routing refresh of series '{}' to Jellyfin service", mediaItem.title());
            return jellyfinTraversalService.refreshSeriesFromJellyfin(mediaItem);
        } else {
            logger.info("Routing refresh of series '{}' to TMDb service", mediaItem.title());
            return tmDbService.refreshSeriesStructure(mediaItem);
        }
    }

    public List<MediaItem> refreshAllStaleSeries() {
        logger.info("Starting batch refresh of all stale series");

        Duration staleThreshold = settingsService.getDurationSetting(
                SettingsService.SYNC_STALE_THRESHOLD,
                Duration.ofDays(2)
        );

        List<MediaItem> staleItems = mediaItemsService.findStaleSeries(staleThreshold);
        logger.info("Found {} stale series to refresh", staleItems.size());

        List<MediaItem> refreshed = new ArrayList<>();
        for (var item : staleItems) {
            try {
                refreshed.add(refresh(item));
            } catch (Exception e) {
                String title = item.title() != null ? item.title() : "Untitled";
                logger.error("Failed to refresh series '{}'", title, e);
            }
        }

        logger.info("Batch refresh complete: {} series refreshed", refreshed.size());
        return refreshed;
    }

    @Async("backgroundTaskExecutor")
    public void refreshAllStaleSeriesAsync() {
        try (var ignored = MdcContext.of(Map.of("jobType", "SERIES_REFRESH"))) {
            logger.info("Starting async batch refresh of all stale series");
            try {
                List<MediaItem> refreshed = refreshAllStaleSeries();
                logger.info("Async batch refresh completed: {} series refreshed", refreshed.size());
            } catch (Exception e) {
                logger.error("Async batch refresh failed", e);
            }
        }
    }
}
