package com.rattatarr.rattatarr.services.schedulers;

import com.rattatarr.rattatarr.models.ArrInstance;
import com.rattatarr.rattatarr.services.SettingsService;
import com.rattatarr.rattatarr.services.SonarrService;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@NullMarked
public class SonarrImportScheduler {
    private static final Logger logger = LoggerFactory.getLogger(SonarrImportScheduler.class);

    private final SonarrService sonarrService;
    private final SettingsService settingsService;
    private final Duration betweenInstancesDelay;

    public SonarrImportScheduler(
            SonarrService sonarrService,
            SettingsService settingsService,
            @Value("${rattatarr.sync.sonarr-between-instances-delay:PT2M}") Duration betweenInstancesDelay) {
        this.sonarrService = sonarrService;
        this.settingsService = settingsService;
        this.betweenInstancesDelay = betweenInstancesDelay;
    }

    @Scheduled(
            fixedDelayString = "${rattatarr.sync.sonarr-interval:PT24H}",
            initialDelayString = "${rattatarr.sync.sonarr-initial-delay:PT10M}"
    )
    public void scheduledSonarrImport() {
        boolean ranPrevious = false;
        for (ArrInstance instance : ArrInstance.values()) {
            if (!settingsService.getBooleanSetting(importEnabledKey(instance), false)) {
                logger.debug("Sonarr ({}) scheduled import is disabled via settings", instance);
                continue;
            }

            sleepIfPreviousInstanceIsRunning(ranPrevious, instance);

            Instant startTime = Instant.now();
            logger.info("Starting scheduled Sonarr ({}) import", instance);

            try {
                sonarrService.runImport(instance);
                Duration duration = Duration.between(startTime, Instant.now());
                logger.info("Scheduled Sonarr ({}) import completed successfully in {} seconds", instance, duration.getSeconds());
                ranPrevious = true;
            } catch (Exception e) {
                logger.error("Scheduled Sonarr ({}) import failed", instance, e);
                ranPrevious = true;
            }
        }
    }

    private void sleepIfPreviousInstanceIsRunning(boolean ranPrevious, ArrInstance instance) {
        if (!ranPrevious) return;
        try {
            logger.debug("Sonarr: waiting {}s before starting {} instance run", betweenInstancesDelay.getSeconds(), instance);
            Thread.sleep(betweenInstancesDelay.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Sonarr inter-instance delay interrupted before {} run", instance);
        }
    }

    private String importEnabledKey(ArrInstance instance) {
        return instance == ArrInstance.ANIME
                ? SettingsService.SYNC_SONARR_ANIME_ENABLED
                : SettingsService.SYNC_SONARR_ENABLED;
    }
}
