package com.rattatarr.rattatarr.services.schedulers;

import com.rattatarr.rattatarr.models.ArrInstance;
import com.rattatarr.rattatarr.services.RadarrService;
import com.rattatarr.rattatarr.services.SettingsService;
import com.rattatarr.rattatarr.utils.MdcContext;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Service
@NullMarked
public class RadarrImportScheduler {
    private static final Logger logger = LoggerFactory.getLogger(RadarrImportScheduler.class);

    private final RadarrService radarrService;
    private final SettingsService settingsService;
    private final Duration betweenInstancesDelay;

    public RadarrImportScheduler(
            RadarrService radarrService,
            SettingsService settingsService,
            @Value("${rattatarr.sync.radarr-between-instances-delay:PT5M}") Duration betweenInstancesDelay) {
        this.radarrService = radarrService;
        this.settingsService = settingsService;
        this.betweenInstancesDelay = betweenInstancesDelay;
    }

    @Scheduled(
            fixedDelayString = "${rattatarr.sync.radarr-interval:PT24H}",
            initialDelayString = "${rattatarr.sync.radarr-initial-delay:PT10M}"
    )
    public void scheduledRadarrImport() {
        try (var ignored = MdcContext.of(Map.of("jobType", "RADARR_IMPORT", "jobSource", "SCHEDULER"))) {
            boolean ranPrevious = false;
            for (ArrInstance instance : ArrInstance.values()) {
                if (!settingsService.getBooleanSetting(importEnabledKey(instance), false)) {
                    logger.debug("Radarr ({}) scheduled import is disabled via settings", instance);
                    continue;
                }

                sleepIfPreviousInstanceIsRunning(ranPrevious, instance);

                Instant startTime = Instant.now();
                logger.info("Starting scheduled Radarr ({}) import", instance);

                try {
                    radarrService.runImport(instance);
                    Duration duration = Duration.between(startTime, Instant.now());
                    logger.info("Scheduled Radarr ({}) import completed successfully in {} seconds", instance, duration.getSeconds());
                    ranPrevious = true;
                } catch (Exception e) {
                    logger.error("Scheduled Radarr ({}) import failed", instance, e);
                    ranPrevious = true;
                }
            }
        }
    }

    @Scheduled(
            fixedDelayString = "${rattatarr.sync.radarr-ratings-interval:PT6H}",
            initialDelayString = "${rattatarr.sync.radarr-ratings-initial-delay:PT15M}"
    )
    public void scheduledRadarrRatingsRefresh() {
        try (var ignored = MdcContext.of(Map.of("jobType", "RADARR_RATINGS_REFRESH", "jobSource", "SCHEDULER"))) {
            boolean ranPrevious = false;
            for (ArrInstance instance : ArrInstance.values()) {
                if (!settingsService.getBooleanSetting(importEnabledKey(instance), false)) {
                    logger.debug("Radarr ({}) ratings refresh is disabled via settings", instance);
                    continue;
                }

                sleepIfPreviousInstanceIsRunning(ranPrevious, instance);

                Instant startTime = Instant.now();
                logger.info("Starting scheduled Radarr ({}) ratings refresh", instance);

                try {
                    radarrService.runRatingsRefresh(instance);
                    Duration duration = Duration.between(startTime, Instant.now());
                    logger.info("Scheduled Radarr ({}) ratings refresh completed successfully in {} seconds", instance, duration.getSeconds());
                    ranPrevious = true;
                } catch (Exception e) {
                    logger.error("Scheduled Radarr ({}) ratings refresh failed", instance, e);
                    ranPrevious = true;
                }
            }
        }
    }

    private void sleepIfPreviousInstanceIsRunning(boolean ranPrevious, ArrInstance instance) {
        if (!ranPrevious) return;
        try {
            logger.debug("Radarr: waiting {}s before starting {} instance run", betweenInstancesDelay.getSeconds(), instance);
            Thread.sleep(betweenInstancesDelay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Radarr inter-instance delay interrupted before {} run", instance);
        }
    }

    private String importEnabledKey(ArrInstance instance) {
        return instance == ArrInstance.ANIME
                ? SettingsService.SYNC_RADARR_ANIME_ENABLED
                : SettingsService.SYNC_RADARR_ENABLED;
    }
}
