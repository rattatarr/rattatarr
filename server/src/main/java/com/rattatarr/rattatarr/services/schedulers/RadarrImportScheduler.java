package com.rattatarr.rattatarr.services.schedulers;

import com.rattatarr.rattatarr.models.ArrInstance;
import com.rattatarr.rattatarr.services.RadarrService;
import com.rattatarr.rattatarr.services.SettingsService;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@NullMarked
public class RadarrImportScheduler {
    private static final Logger logger = LoggerFactory.getLogger(RadarrImportScheduler.class);

    private final RadarrService radarrService;
    private final SettingsService settingsService;

    public RadarrImportScheduler(RadarrService radarrService, SettingsService settingsService) {
        this.radarrService = radarrService;
        this.settingsService = settingsService;
    }

    @Scheduled(
            fixedDelayString = "${rattatarr.sync.radarr-interval:PT24H}",
            initialDelayString = "${rattatarr.sync.radarr-initial-delay:PT10M}"
    )
    public void scheduledRadarrImport() {
        for (ArrInstance instance : ArrInstance.values()) {
            if (!settingsService.getBooleanSetting(importEnabledKey(instance), false)) {
                logger.debug("Radarr ({}) scheduled import is disabled via settings", instance);
                continue;
            }

            Instant startTime = Instant.now();
            logger.info("Starting scheduled Radarr ({}) import", instance);

            try {
                radarrService.runImport(instance);
                Duration duration = Duration.between(startTime, Instant.now());
                logger.info("Scheduled Radarr ({}) import completed successfully in {} seconds", instance, duration.getSeconds());
            } catch (Exception e) {
                logger.error("Scheduled Radarr ({}) import failed", instance, e);
            }
        }
    }

    @Scheduled(
            fixedDelayString = "${rattatarr.sync.radarr-ratings-interval:PT6H}",
            initialDelayString = "${rattatarr.sync.radarr-ratings-initial-delay:PT15M}"
    )
    public void scheduledRadarrRatingsRefresh() {
        for (ArrInstance instance : ArrInstance.values()) {
            if (!settingsService.getBooleanSetting(importEnabledKey(instance), false)) {
                logger.debug("Radarr ({}) ratings refresh is disabled via settings", instance);
                continue;
            }

            Instant startTime = Instant.now();
            logger.info("Starting scheduled Radarr ({}) ratings refresh", instance);

            try {
                radarrService.runRatingsRefresh(instance);
                Duration duration = Duration.between(startTime, Instant.now());
                logger.info("Scheduled Radarr ({}) ratings refresh completed successfully in {} seconds", instance, duration.getSeconds());
            } catch (Exception e) {
                logger.error("Scheduled Radarr ({}) ratings refresh failed", instance, e);
            }
        }
    }

    private String importEnabledKey(ArrInstance instance) {
        return instance == ArrInstance.ANIME
                ? SettingsService.SYNC_RADARR_ANIME_ENABLED
                : SettingsService.SYNC_RADARR_ENABLED;
    }
}
