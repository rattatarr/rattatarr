package com.rattatarr.rattatarr.services.schedulers;

import com.rattatarr.rattatarr.models.ArrInstance;
import com.rattatarr.rattatarr.services.SettingsService;
import com.rattatarr.rattatarr.services.SonarrService;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public SonarrImportScheduler(SonarrService sonarrService, SettingsService settingsService) {
        this.sonarrService = sonarrService;
        this.settingsService = settingsService;
    }

    @Scheduled(
            fixedDelayString = "${rattatarr.sync.sonarr-interval:PT24H}",
            initialDelayString = "${rattatarr.sync.sonarr-initial-delay:PT10M}"
    )
    public void scheduledSonarrImport() {
        for (ArrInstance instance : ArrInstance.values()) {
            if (!settingsService.getBooleanSetting(importEnabledKey(instance), false)) {
                logger.debug("Sonarr ({}) scheduled import is disabled via settings", instance);
                continue;
            }

            Instant startTime = Instant.now();
            logger.info("Starting scheduled Sonarr ({}) import", instance);

            try {
                sonarrService.runImport(instance);
                Duration duration = Duration.between(startTime, Instant.now());
                logger.info("Scheduled Sonarr ({}) import completed successfully in {} seconds", instance, duration.getSeconds());
            } catch (Exception e) {
                logger.error("Scheduled Sonarr ({}) import failed", instance, e);
            }
        }
    }

    private String importEnabledKey(ArrInstance instance) {
        return instance == ArrInstance.ANIME
                ? SettingsService.SYNC_SONARR_ANIME_ENABLED
                : SettingsService.SYNC_SONARR_ENABLED;
    }
}
