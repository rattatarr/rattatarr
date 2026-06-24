package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.radarr.RadarrClient;
import com.rattatarr.rattatarr.clients.radarr.responses.RadarrInternalMovieResponseDTO;
import com.rattatarr.rattatarr.clients.radarr.responses.RadarrMovieLookupResponseDTO;
import com.rattatarr.rattatarr.clients.radarr.responses.RadarrRatings;
import com.rattatarr.rattatarr.models.ArrInstance;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.BackgroundJob;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.utils.MdcContext;
import com.rattatarr.rattatarr.utils.ParallelAPIProcessor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;

@Service
@NullMarked
public class RadarrService {
    private static final Logger logger = LoggerFactory.getLogger(RadarrService.class);

    private final RadarrClient defaultClient;
    private final RadarrClient animeClient;
    private final TMDbService tmDbService;
    private final MediaItemMetadataService mediaItemMetadataService;
    private final MediaItemsService mediaItemsService;
    private final BackgroundJobService backgroundJobService;
    private final Executor tmdbApiExecutor;

    public RadarrService(
            @Qualifier("radarrDefaultClient") RadarrClient defaultClient,
            @Qualifier("radarrAnimeClient") RadarrClient animeClient,
            TMDbService tmDbService,
            MediaItemMetadataService mediaItemMetadataService,
            MediaItemsService mediaItemsService,
            BackgroundJobService backgroundJobService,
            @Qualifier("tmdbApiExecutor") Executor tmdbApiExecutor) {
        this.defaultClient = defaultClient;
        this.animeClient = animeClient;
        this.tmDbService = tmDbService;
        this.mediaItemMetadataService = mediaItemMetadataService;
        this.mediaItemsService = mediaItemsService;
        this.backgroundJobService = backgroundJobService;
        this.tmdbApiExecutor = tmdbApiExecutor;
    }

    private RadarrClient clientFor(ArrInstance instance) {
        return instance == ArrInstance.ANIME ? animeClient : defaultClient;
    }

    public boolean testConnection(ArrInstance instance) {
        return clientFor(instance).testConnection();
    }

    public RadarrMovieLookupResponseDTO lookupByTmdbId(int tmdbId, ArrInstance instance) {
        return clientFor(instance).lookupByTmdbId(tmdbId);
    }

    public void enrichMovieFromRadarrIfStale(UUID mediaItemId) {
        // We don't care which instance we use for enrichment. Radarr uses TMDbID and that doesn't depends on any type of instance
        enrichMovieFromRadarrIfStale(mediaItemId, ArrInstance.DEFAULT);
    }

    public void enrichMovieFromRadarrIfStale(UUID mediaItemId, ArrInstance instance) {
        var client = clientFor(instance);
        if (!client.isConfigured()) return;

        var mediaItemOpt = mediaItemsService.findById(mediaItemId);
        if (mediaItemOpt.isEmpty()) return;
        var mediaItem = mediaItemOpt.get();

        if (mediaItem.TMDbId() == null) return;
        if (mediaItemMetadataService.isRatingFresh(mediaItemId)) return;

        try {
            int tmdbId = Integer.parseInt(mediaItem.TMDbId());
            var movie = lookupByTmdbId(tmdbId, instance);

            if (movie == null) {
                logger.warn("No Radarr ({}) movie found for TMDb ID {}, cannot enrich '{}' ({})",
                        instance, tmdbId, mediaItem.title(), mediaItemId);
                return;
            }

            if (movie.tmdbId() == null) {
                logger.warn("Radarr ({}) lookup for TMDb ID {} returned movie '{}' with no TMDb ID, skipping", instance, tmdbId, movie.title());
                return;
            }

            var ratings = extractRatings(movie.ratings());
            mediaItemMetadataService.updateExternalRatings(mediaItem, ratings.imdbRating(), ratings.rottenTomatoesRating());
            logger.info("Radarr ({}) ratings refreshed for movie '{}' ({})", instance, mediaItem.title(), mediaItemId);
        } catch (Exception e) {
            logger.warn("Failed to enrich movie '{}' ({}) from Radarr ({}): {}",
                    mediaItem.title(), mediaItemId, instance, e.getMessage());
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

    public void enrichAllMoviesWithRadarrRatings(ArrInstance instance) {
        var movies = mediaItemsService.findAllMoviesWithTmdbId();
        ParallelAPIProcessor.processInParallel(
                movies,
                movie -> fetchRadarrRatings(movie, instance),
                this::applyRadarrRatings,
                tmdbApiExecutor,
                logger,
                "Radarr ratings refresh"
        );
    }

    @Nullable
    private MovieEnrichmentResult fetchRadarrRatings(MediaItem movie, ArrInstance instance) {
        UUID mediaItemId = movie.id();
        if (mediaItemId == null) return null;
        if (mediaItemMetadataService.isRatingFresh(mediaItemId)) return null;
        try {
            int tmdbId = Integer.parseInt(movie.TMDbId());
            var lookup = clientFor(instance).lookupByTmdbId(tmdbId);
            if (lookup == null || lookup.tmdbId() == null) return null;
            return new MovieEnrichmentResult(movie, lookup);
        } catch (Exception e) {
            logger.warn("Radarr ({}) lookup failed for movie '{}' ({}): {}",
                    instance, movie.title(), mediaItemId, e.getMessage());
            return null;
        }
    }

    private void applyRadarrRatings(MovieEnrichmentResult result) {
        var ratings = extractRatings(result.lookup().ratings());
        mediaItemMetadataService.updateExternalRatings(result.movie(), ratings.imdbRating(), ratings.rottenTomatoesRating());
        logger.info("Radarr ratings refreshed for movie '{}' ({})", result.movie().title(), result.movie().id());
    }

    public void runRatingsRefresh(ArrInstance instance) {
        if (!clientFor(instance).isConfigured()) {
            logger.debug("Radarr ({}) not configured, skipping ratings refresh", instance);
            return;
        }
        enrichAllMoviesWithRadarrRatings(instance);
    }

    public void runImport(ArrInstance instance) {
        var client = clientFor(instance);
        if (!client.isConfigured()) {
            logger.debug("Radarr ({}) not configured, skipping import", instance);
            return;
        }
        importAllMovies(client.getMonitoredInternalMovies(null));
    }

    @Async("backgroundTaskExecutor")
    public void triggerBackgroundImport(BackgroundJob job, ArrInstance instance) {
        try (var ignored = MdcContext.of(Map.of("jobId", job.id().toString(), "jobType", job.type().name()))) {
            try {
                logger.info("Radarr ({}) import started, jobId={}", instance, job.id());
                backgroundJobService.markRunning(job);
                runImport(instance);
                backgroundJobService.markCompleted(job, "Radarr import completed successfully");
                logger.info("Radarr ({}) import completed, jobId={}", instance, job.id());
            } catch (Exception error) {
                backgroundJobService.markFailed(job, error.getMessage());
                logger.error("Radarr ({}) import failed, jobId={}", instance, job.id(), error);
            }
        }
    }

    @Async("backgroundTaskExecutor")
    public void triggerBackgroundRatingsRefresh(BackgroundJob job, ArrInstance instance) {
        try (var ignored = MdcContext.of(Map.of("jobId", job.id().toString(), "jobType", job.type().name()))) {
            try {
                logger.info("Radarr ({}) ratings refresh started, jobId={}", instance, job.id());
                backgroundJobService.markRunning(job);
                runRatingsRefresh(instance);
                backgroundJobService.markCompleted(job, "Radarr ratings refresh completed successfully");
                logger.info("Radarr ({}) ratings refresh completed, jobId={}", instance, job.id());
            } catch (Exception error) {
                backgroundJobService.markFailed(job, error.getMessage());
                logger.error("Radarr ({}) ratings refresh failed, jobId={}", instance, job.id(), error);
            }
        }
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
