package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.radarr.RadarrClient;
import com.rattatarr.rattatarr.clients.radarr.responses.RadarrInternalMovieResponseDTO;
import com.rattatarr.rattatarr.clients.radarr.responses.RadarrMovieLookupResponseDTO;
import com.rattatarr.rattatarr.clients.radarr.responses.RadarrRatings;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.BackgroundJob;
import com.rattatarr.rattatarr.models.entities.MediaItem;
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

    public RadarrMovieLookupResponseDTO lookupByTmdbId(int tmdbId) {
        return radarrClient.lookupByTmdbId(tmdbId);
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
            var movie = lookupByTmdbId(tmdbId);

            if (movie == null) {
                logger.warn("No Radarr movie found for TMDb ID {}, cannot enrich MediaItem ID {}", tmdbId, mediaItemId);
                return;
            }

            if (movie.tmdbId() == null) {
                logger.warn("Radarr lookup for TMDb ID {} returned movie '{}' with no TMDb ID, skipping", tmdbId, movie.title());
                return;
            }

            var ratings = extractRatings(movie.ratings());
            mediaItemMetadataService.updateExternalRatings(mediaItem, ratings.imdbRating(), ratings.rottenTomatoesRating());
            logger.info("Radarr ratings refreshed for MediaItem ID: {}", mediaItemId);
        } catch (Exception e) {
            logger.warn("Failed to enrich movie {} from Radarr: {}", mediaItemId, e.getMessage());
        }
    }

    @Nullable
    private RadarrImportItem buildRadarrImportItem(RadarrInternalMovieResponseDTO movie) {
        if (movie.tmdbId() == null) {
            logger.warn("Radarr movie '{}' (id={}) has no TMDb ID, skipping", movie.title(), movie.id());
            return null;
        }
        var ratings = extractRatings(movie.ratings());
        return new RadarrImportItem(movie.tmdbId().toString(), ratings.imdbRating(), ratings.rottenTomatoesRating());
    }

    private ExtractedRatings extractRatings(@Nullable RadarrRatings ratings) {
        if (ratings == null) return new ExtractedRatings(null, null);
        Float imdbRating = null;
        Integer rottenTomatoesRating = null;
        if (ratings.imdb() != null) {
            imdbRating = Math.round((float) ratings.imdb().value() * 100f) / 100f;
        }
        if (ratings.rottenTomatoes() != null) {
            rottenTomatoesRating = (int) ratings.rottenTomatoes().value();
        }
        return new ExtractedRatings(imdbRating, rottenTomatoesRating);
    }

    private void importAndUpdateRatings(RadarrImportItem item) {
        var mediaItem = tmDbService.importMediaItem(item.tmdbId(), MediaType.MOVIE);
        mediaItemMetadataService.updateExternalRatings(mediaItem, item.imdbRating(), item.rottenTomatoesRating());
    }

    public void importAllMovies(List<RadarrInternalMovieResponseDTO> movies) {
        ParallelAPIProcessor.processInParallel(
                movies,
                this::buildRadarrImportItem,
                this::importAndUpdateRatings,
                tmdbApiExecutor,
                logger,
                "Radarr movies"
        );
    }

    public void enrichAllMoviesWithRadarrRatings() {
        var movies = mediaItemsService.findAllMoviesWithTmdbId();
        ParallelAPIProcessor.processInParallel(
                movies,
                this::fetchRadarrRatings,
                this::applyRadarrRatings,
                tmdbApiExecutor,
                logger,
                "Radarr ratings refresh"
        );
    }

    @Nullable
    private MovieEnrichmentResult fetchRadarrRatings(MediaItem movie) {
        UUID mediaItemId = movie.id();
        if (mediaItemId == null) return null;
        if (mediaItemMetadataService.isRatingFresh(mediaItemId)) return null;
        try {
            int tmdbId = Integer.parseInt(movie.TMDbId());
            var lookup = radarrClient.lookupByTmdbId(tmdbId);
            if (lookup == null || lookup.tmdbId() == null) return null;
            return new MovieEnrichmentResult(movie, lookup);
        } catch (Exception e) {
            logger.warn("Radarr lookup failed for MediaItem {}: {}", mediaItemId, e.getMessage());
            return null;
        }
    }

    private void applyRadarrRatings(MovieEnrichmentResult result) {
        var ratings = extractRatings(result.lookup().ratings());
        mediaItemMetadataService.updateExternalRatings(result.movie(), ratings.imdbRating(), ratings.rottenTomatoesRating());
        logger.info("Radarr ratings refreshed for MediaItem ID: {}", result.movie().id());
    }

    public void runRatingsRefresh() {
        if (!radarrClient.isConfigured()) {
            logger.debug("Radarr not configured, skipping ratings refresh");
            return;
        }
        enrichAllMoviesWithRadarrRatings();
    }

    public void runImport() {
        if (!radarrClient.isConfigured()) {
            logger.debug("Radarr not configured, skipping import");
            return;
        }
        importAllMovies(getTrackedMovies());
    }

    @Async("backgroundTaskExecutor")
    public void triggerBackgroundImport(BackgroundJob job) {
        logger.info("Radarr import started, jobId={}", job.id());
        backgroundJobService.markRunning(job);
        try {
            runImport();
            backgroundJobService.markCompleted(job, "Radarr import completed successfully");
            logger.info("Radarr import completed, jobId={}", job.id());
        } catch (Exception error) {
            backgroundJobService.markFailed(job, error.getMessage());
            logger.error("Radarr import failed, jobId={}", job.id(), error);
        }
    }

    @Async("backgroundTaskExecutor")
    public void triggerBackgroundRatingsRefresh(BackgroundJob job) {
        logger.info("Radarr ratings refresh started, jobId={}", job.id());
        backgroundJobService.markRunning(job);
        try {
            runRatingsRefresh();
            backgroundJobService.markCompleted(job, "Radarr ratings refresh completed successfully");
            logger.info("Radarr ratings refresh completed, jobId={}", job.id());
        } catch (Exception error) {
            backgroundJobService.markFailed(job, error.getMessage());
            logger.error("Radarr ratings refresh failed, jobId={}", job.id(), error);
        }
    }

    private List<RadarrInternalMovieResponseDTO> getTrackedMovies() {
        return radarrClient.getMonitoredInternalMovies(null);
    }

    private record MovieEnrichmentResult(
            MediaItem movie,
            RadarrMovieLookupResponseDTO lookup
    ) {
    }

    private record RadarrImportItem(
            String tmdbId,
            @Nullable Float imdbRating,
            @Nullable Integer rottenTomatoesRating
    ) {
    }

    private record ExtractedRatings(
            @Nullable Float imdbRating,
            @Nullable Integer rottenTomatoesRating
    ) {
    }
}
