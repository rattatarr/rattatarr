package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.sonarr.SonarrClient;
import com.rattatarr.rattatarr.clients.sonarr.responses.SonarrSeriesResponseDTO;
import com.rattatarr.rattatarr.models.ArrInstance;
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

    private final SonarrClient defaultClient;
    private final SonarrClient animeClient;
    private final TMDbService tmDbService;
    private final BackgroundJobService backgroundJobService;
    private final Executor tmdbApiExecutor;

    public SonarrService(
            @Qualifier("sonarrDefaultClient") SonarrClient defaultClient,
            @Qualifier("sonarrAnimeClient") SonarrClient animeClient,
            TMDbService tmDbService,
            BackgroundJobService backgroundJobService,
            @Qualifier("tmdbApiExecutor") Executor tmdbApiExecutor) {
        this.defaultClient = defaultClient;
        this.animeClient = animeClient;
        this.tmDbService = tmDbService;
        this.backgroundJobService = backgroundJobService;
        this.tmdbApiExecutor = tmdbApiExecutor;
    }

    private SonarrClient clientFor(ArrInstance instance) {
        return instance == ArrInstance.ANIME ? animeClient : defaultClient;
    }

    public boolean testConnection(ArrInstance instance) {
        return clientFor(instance).testConnection();
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

    public void runImport(ArrInstance instance) {
        var client = clientFor(instance);
        if (!client.isConfigured()) {
            logger.debug("Sonarr ({}) not configured, skipping import", instance);
            return;
        }
        importAllSeries(client.getMonitoredInternalSeries());
    }

    @Async("backgroundTaskExecutor")
    public void triggerBackgroundImport(BackgroundJob job, ArrInstance instance) {
        logger.info("Sonarr ({}) import started, jobId={}", instance, job.id());
        backgroundJobService.markRunning(job);
        try {
            runImport(instance);
            backgroundJobService.markCompleted(job, "Sonarr import completed successfully");
            logger.info("Sonarr ({}) import completed, jobId={}", instance, job.id());
        } catch (Exception error) {
            backgroundJobService.markFailed(job, error.getMessage());
            logger.error("Sonarr ({}) import failed, jobId={}", instance, job.id(), error);
        }
    }
}
