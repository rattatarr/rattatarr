package com.rattatarr.rattatarr.services.schedulers;

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
        boolean enabled = settingsService.getBooleanSetting(
                SettingsService.SYNC_RADARR_ENABLED,
                false
        );

        if (!enabled) {
            logger.debug("Radarr scheduled import is disabled via settings");
            return;
        }

        Instant startTime = Instant.now();
        logger.info("Starting scheduled Radarr import");

        try {
            radarrService.runImport();

            Duration duration = Duration.between(startTime, Instant.now());
            logger.info("Scheduled Radarr import completed successfully in {} seconds",
                    duration.getSeconds());
        } catch (Exception e) {
            logger.error("Scheduled Radarr import failed", e);
        }
    }
}
