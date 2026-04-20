package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.radarr.RadarrClient;
import com.rattatarr.rattatarr.clients.radarr.responses.RadarrMovieResponseDTO;
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
import java.util.UUID;
import java.util.concurrent.Executor;

@Service
@NullMarked
public class RadarrService {
    private static final Logger logger = LoggerFactory.getLogger(RadarrService.class);

    private final RadarrClient radarrClient;
    private final TMDbService tmDbService;
    private final MediaItemMetadataService mediaItemMetadataService;
    private final MediaItemsService mediaItemsService;
    private final BackgroundJobService backgroundJobService;
    private final Executor tmdbApiExecutor;

    public RadarrService(
            RadarrClient radarrClient,
            TMDbService tmDbService,
            MediaItemMetadataService mediaItemMetadataService,
            MediaItemsService mediaItemsService,
            BackgroundJobService backgroundJobService,
            @Qualifier("tmdbApiExecutor") Executor tmdbApiExecutor) {
        this.radarrClient = radarrClient;
        this.tmDbService = tmDbService;
        this.mediaItemMetadataService = mediaItemMetadataService;
        this.mediaItemsService = mediaItemsService;
        this.backgroundJobService = backgroundJobService;
        this.tmdbApiExecutor = tmdbApiExecutor;
    }

    public List<RadarrMovieResponseDTO> getTrackedMovies(@Nullable Integer tmdbId) {
        return radarrClient.getMovies(tmdbId);
    }

    public void enrichMovieFromRadarrIfStale(UUID mediaItemId) {
        if (!radarrClient.isConfigured()) return;

        var mediaItemOpt = mediaItemsService.findById(mediaItemId);
        if (mediaItemOpt.isEmpty()) return;
        var mediaItem = mediaItemOpt.get();

        if (mediaItem.TMDbId() == null) return;
        if (mediaItemMetadataService.isRatingFresh(mediaItemId)) return;

        try {
            int tmdbId = Integer.parseInt(mediaItem.TMDbId());
            List<RadarrMovieResponseDTO> movies = getTrackedMovies(tmdbId);
            if (movies.isEmpty()) return;

            RadarrImportItem item = buildRadarrImportItem(movies.getFirst());
            if (item == null) return;

            mediaItemMetadataService.updateExternalRatings(mediaItem, item.imdbRating(), item.rottenTomatoesRating());
            logger.info("Radarr ratings refreshed for MediaItem ID: {}", mediaItemId);
        } catch (Exception e) {
            logger.warn("Failed to enrich movie {} from Radarr: {}", mediaItemId, e.getMessage());
        }
    }

    @Nullable
    private RadarrImportItem buildRadarrImportItem(RadarrMovieResponseDTO movie) {
        if (movie.tmdbId() == null) {
            logger.warn("Radarr movie '{}' (id={}) has no TMDb ID, skipping", movie.title(), movie.id());
            return null;
        }

        Float imdbRating = null;
        Integer rottenTomatoesRating = null;

        if (movie.ratings() != null) {
            if (movie.ratings().imdb() != null) {
                imdbRating = Math.round((float) movie.ratings().imdb().value() * 100f) / 100f;
            }
            if (movie.ratings().rottenTomatoes() != null) {
                rottenTomatoesRating = (int) movie.ratings().rottenTomatoes().value();
            }
        }

        return new RadarrImportItem(movie.tmdbId().toString(), imdbRating, rottenTomatoesRating);
    }

    private void importAndUpdateRatings(RadarrImportItem item) {
        var mediaItem = tmDbService.importMediaItem(item.tmdbId(), MediaType.MOVIE);
        mediaItemMetadataService.updateExternalRatings(mediaItem, item.imdbRating(), item.rottenTomatoesRating());
    }

    public void importAllMovies(List<RadarrMovieResponseDTO> movies) {
        ParallelAPIProcessor.processInParallel(
                movies,
                this::buildRadarrImportItem,
                this::importAndUpdateRatings,
                tmdbApiExecutor,
                logger,
                "Radarr movies"
        );
    }

    @Async("backgroundTaskExecutor")
    public void triggerBackgroundImport(BackgroundJob job) {
        logger.info("Radarr import started, jobId={}", job.id());
        backgroundJobService.markRunning(job);
        try {
            List<RadarrMovieResponseDTO> movies = radarrClient.getMovies(null);
            importAllMovies(movies);
            backgroundJobService.markCompleted(job, "Radarr import completed successfully");
            logger.info("Radarr import completed, jobId={}", job.id());
        } catch (Exception error) {
            backgroundJobService.markFailed(job, error.getMessage());
            logger.error("Radarr import failed, jobId={}", job.id(), error);
        }
    }

    private record RadarrImportItem(
            String tmdbId,
            @Nullable Float imdbRating,
            @Nullable Integer rottenTomatoesRating
    ) {
    }
}
