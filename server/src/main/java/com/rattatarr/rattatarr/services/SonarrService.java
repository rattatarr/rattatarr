package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.sonarr.SonarrClient;
import com.rattatarr.rattatarr.clients.sonarr.responses.SonarrSeriesResponseDTO;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.BackgroundJob;
import com.rattatarr.rattatarr.utils.ParallelAPIProcessor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executor;

@Service
@NullMarked
public class SonarrService {
    private static final Logger logger = LoggerFactory.getLogger(SonarrService.class);

    private final SonarrClient sonarrClient;
    private final TMDbService tmDbService;
    private final BackgroundJobService backgroundJobService;
    private final Executor tmdbApiExecutor;

    public SonarrService(
            SonarrClient sonarrClient,
            TMDbService tmDbService,
            BackgroundJobService backgroundJobService,
            @Qualifier("tmdbApiExecutor") Executor tmdbApiExecutor) {
        this.sonarrClient = sonarrClient;
        this.tmDbService = tmDbService;
        this.backgroundJobService = backgroundJobService;
        this.tmdbApiExecutor = tmdbApiExecutor;
    }

    public boolean testConnection() {
        return sonarrClient.testConnection();
    }

    @Nullable
    private String extractTmdbId(SonarrSeriesResponseDTO series) {
        if (series.tmdbId() == null) {
            logger.warn("Sonarr series '{}' (id={}) has no TMDb ID, skipping", series.title(), series.id());
            return null;
        }
        return series.tmdbId().toString();
    }

    private void importSeries(String tmdbId) {
        tmDbService.importMediaItem(tmdbId, MediaType.SERIES);
    }

    public void importAllSeries(List<SonarrSeriesResponseDTO> series) {
        ParallelAPIProcessor.processInParallel(
                series,
                this::extractTmdbId,
                this::importSeries,
                tmdbApiExecutor,
                logger,
                "Sonarr series"
        );
    }

    public void runImport() {
        if (!sonarrClient.isConfigured()) {
            logger.debug("Sonarr not configured, skipping import");
            return;
        }
        importAllSeries(sonarrClient.getMonitoredInternalSeries());
    }

    @Async("backgroundTaskExecutor")
    public void triggerBackgroundImport(BackgroundJob job) {
        logger.info("Sonarr import started, jobId={}", job.id());
        backgroundJobService.markRunning(job);
        try {
            runImport();
            backgroundJobService.markCompleted(job, "Sonarr import completed successfully");
            logger.info("Sonarr import completed, jobId={}", job.id());
        } catch (Exception error) {
            backgroundJobService.markFailed(job, error.getMessage());
            logger.error("Sonarr import failed, jobId={}", job.id(), error);
        }
    }
}
