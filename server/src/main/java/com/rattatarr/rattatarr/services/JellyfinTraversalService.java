package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.jellyfin.JellyfinClient;
import com.rattatarr.rattatarr.clients.jellyfin.requests.queries.JellyfinItemsQueryBuilder;
import com.rattatarr.rattatarr.clients.jellyfin.responses.*;
import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.exceptions.JellyfinTraversalExceptions;
import com.rattatarr.rattatarr.models.JellyfinMediaType;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.MediaValidationResult;
import com.rattatarr.rattatarr.models.entities.*;
import com.rattatarr.rattatarr.utils.ValueResolver;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@NullMarked
public class JellyfinTraversalService {
    private static final Logger logger = LoggerFactory.getLogger(JellyfinTraversalService.class);

    private static final EnumSet<JellyfinMediaType> FOLDER_TYPES = EnumSet.of(JellyfinMediaType.FOLDER);
    private static final EnumSet<JellyfinMediaType> MOVIE_TYPES = EnumSet.of(JellyfinMediaType.MOVIE);
    private static final EnumSet<JellyfinMediaType> SERIES_TYPES = EnumSet.of(JellyfinMediaType.SERIES);
    private static final EnumSet<JellyfinMediaType> SEASON_TYPES = EnumSet.of(JellyfinMediaType.SEASON);
    private static final EnumSet<JellyfinMediaType> EPISODE_TYPES = EnumSet.of(JellyfinMediaType.EPISODE);

    private final JellyfinClient jellyfinClient;
    private final GenresService genresService;
    private final MediaItemsService mediaItemsService;
    private final MediaSeasonsService mediaSeasonsService;
    private final MediaEpisodesService mediaEpisodesService;
    private final BrokenMediaItemsService brokenMediaItemsService;
    private final MediaItemMetadataService mediaItemMetadataService;
    private final MediaItemCreditsService mediaItemCreditsService;
    private final BackgroundJobService backgroundJobService;

    public JellyfinTraversalService(JellyfinClient jellyfinClient,
                                    GenresService genresService,
                                    MediaItemsService mediaItemsService,
                                    MediaSeasonsService mediaSeasonsService,
                                    MediaEpisodesService mediaEpisodesService,
                                    BrokenMediaItemsService brokenMediaItemsService,
                                    MediaItemMetadataService mediaItemMetadataService,
                                    MediaItemCreditsService mediaItemCreditsService,
                                    BackgroundJobService backgroundJobService
    ) {
        this.jellyfinClient = jellyfinClient;
        this.genresService = genresService;
        this.mediaItemsService = mediaItemsService;
        this.mediaSeasonsService = mediaSeasonsService;
        this.mediaEpisodesService = mediaEpisodesService;
        this.brokenMediaItemsService = brokenMediaItemsService;
        this.mediaItemMetadataService = mediaItemMetadataService;
        this.mediaItemCreditsService = mediaItemCreditsService;
        this.backgroundJobService = backgroundJobService;
    }

    private <T extends JellyfinClientItemResponseDTO> List<T> filterByTypes(
            List<JellyfinClientItemResponseDTO> items, EnumSet<JellyfinMediaType> allowed, Class<T> type) {
        return items.stream()
                .filter(item -> allowed.contains(item.mediaType()))
                .map(type::cast)
                .toList();
    }

    private Set<Genre> resolveGenres(List<String> genreNames) {
        return genreNames.stream()
                .distinct()
                .map(genresService::findOrCreateByNameCached)
                .collect(Collectors.toSet());
    }

    private MediaValidationResult validateMediaItem(@Nullable String TMDbId, @Nullable String IMDbId, @Nullable Integer productionYear, @Nullable List<String> genres) {
        List<String> missingFields = new ArrayList<>();

        if (!StringUtils.hasText(TMDbId)) missingFields.add("TMDbId");
        if (!StringUtils.hasText(IMDbId)) missingFields.add("IMDbId");
        if (productionYear == null) missingFields.add("ProductionYear");
        if (ObjectUtils.isEmpty(genres)) missingFields.add("Genres");

        boolean isValid = missingFields.isEmpty();
        String missingFieldsStr = String.join(", ", missingFields);

        return new MediaValidationResult(isValid, missingFieldsStr);
    }

    // ===== FETCH METHODS =====
    private List<JellyfinClientItemFolderResponseDTO> fetchFolders() {
        logger.info("Starting to fetch folders from Jellyfin");
        var wrapper = jellyfinClient.getItems(
                JellyfinItemsQueryBuilder.builder()
                        .filter("isFolder")
                        .build()
        );
        return filterByTypes(wrapper.items(), FOLDER_TYPES, JellyfinClientItemFolderResponseDTO.class);
    }

    private List<JellyfinClientItemMovieResponseDTO> fetchMovies(String parentId) {
        logger.info("Fetching movies under parent ID: {}", parentId);
        var wrapper = jellyfinClient.getItems(
                JellyfinItemsQueryBuilder.builder()
                        .parentId(parentId)
                        .isMovie(true)
                        .filter("IsNotFolder")
                        .field("ProviderIds")
                        .field("Genres")
                        .field("ItemCounts")
                        .build()
        );
        return filterByTypes(wrapper.items(), MOVIE_TYPES, JellyfinClientItemMovieResponseDTO.class);
    }

    private List<JellyfinClientItemSeriesResponseDTO> fetchSeries(String parentId) {
        logger.info("Fetching series under parent ID: {}", parentId);
        var wrapper = jellyfinClient.getItems(
                JellyfinItemsQueryBuilder.builder()
                        .parentId(parentId)
                        .isSeries(true)
                        .filter("IsFolder")
                        .field("ProviderIds")
                        .field("Genres")
                        .field("ItemCounts")
                        .build()
        );
        return filterByTypes(wrapper.items(), SERIES_TYPES, JellyfinClientItemSeriesResponseDTO.class);
    }

    private List<JellyfinClientItemSeasonResponseDTO> fetchSeasons(String seriesId) {
        logger.info("Fetching seasons for series ID: {}", seriesId);
        var wrapper = jellyfinClient.getItems(
                JellyfinItemsQueryBuilder.builder()
                        .parentId(seriesId)
                        .filter("IsFolder")
                        .build()
        );
        return filterByTypes(wrapper.items(), SEASON_TYPES, JellyfinClientItemSeasonResponseDTO.class);
    }

    private List<JellyfinClientItemEpisodeResponseDTO> fetchEpisodes(String seasonId) {
        logger.info("Fetching episodes for season ID: {}", seasonId);
        var wrapper = jellyfinClient.getItems(
                JellyfinItemsQueryBuilder.builder()
                        .parentId(seasonId)
                        .filter("IsNotFolder")
                        .build()
        );
        return filterByTypes(wrapper.items(), EPISODE_TYPES, JellyfinClientItemEpisodeResponseDTO.class);
    }
    // ===== END FETCH METHODS ======

    // ===== SYNC METHODS =====

    /**
     * If a movie is already present in the database (by Jellyfin ID), it will be skipped.
     * That means no new genres will be added to existing movies.
     * Maybe extended in the future to update existing movies with new genres, but not sure if this will be used.
     * Sounds nice but might not be worth the effort.
     * <p>
     * If a movie is missing critical metadata (TMDb ID, IMDb ID, production year, genres),
     * it will be recorded in the BrokenMediaItems table for later review.
     */
    private void processMovies(String folderId) {
        List<JellyfinClientItemMovieResponseDTO> movies = fetchMovies(folderId);
        List<MediaItem> toSave = new ArrayList<>();

        for (var movie : movies) {
            MediaValidationResult validation = validateMediaItem(
                    movie.providers().TMDbId(),
                    movie.providers().IMDbId(),
                    movie.productionYear(),
                    movie.genres()
            );

            if (!validation.isValid()) {
                if (brokenMediaItemsService.findByJellyfinId(movie.id()).isEmpty()) {
                    BrokenMediaItem broken = new BrokenMediaItem(
                            MediaType.MOVIE,
                            movie.name(),
                            movie.id(),
                            movie.providers().TMDbId(),
                            movie.providers().IMDbId(),
                            movie.productionYear(),
                            validation.missingFields()
                    );
                    brokenMediaItemsService.saveBrokenMediaItem(broken);
                }
                continue;
            }

            // Skip if already exists
            if (mediaItemsService.findByJellyfinId(movie.id()).isPresent()) {
                continue;
            }

            // Sometimes the jellyfin ID seems to change, so we rely on tmdb present more often than imdb ( always ? ) and it is unique in our DB.
            // Update to the new jellyfin id.
            Optional<MediaItem> movieByTMDbId = mediaItemsService.findByTMDbId(movie.providers().TMDbId());

            if (movieByTMDbId.isPresent()) {
                MediaItem resolvedMovie = movieByTMDbId.get();
                resolvedMovie.setJellyfinId(movie.id());
                mediaItemsService.update(
                        resolvedMovie.id(),
                        resolvedMovie
                );
                logger.info("Updated Jellyfin ID for movie '{}' (TMDb ID: {}) to new Jellyfin ID: {}",
                        movie.name(), movie.providers().TMDbId(), movie.id());
            }

            Set<Genre> genreSet = resolveGenres(movie.genres());
            toSave.add(new MediaItem(
                    MediaType.MOVIE,
                    movie.name(),
                    movie.id(),
                    movie.providers().TMDbId(),
                    movie.providers().IMDbId(),
                    movie.productionYear(),
                    ValueResolver.runTimeTicksToMinutes(movie.runTimeTicks()),
                    genreSet,
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptySet()
            ));
        }

        try {
            if (!toSave.isEmpty()) {
                mediaItemsService.saveBatch(toSave);
            }
        } catch (Exception ue) {
            // In case that the batch fails, try to salvage and save one by one, log the culprit
            for (var item : toSave) {
                try {
                    mediaItemsService.save(item);
                } catch (Exception e) {
                    logger.error("Failed to save media movie item with values   - title: {}, jellyfinId: {}, TMDbId: {}, IMDbId: {}, productionYear: {}",
                            item.title(), item.jellyfinId(), item.TMDbId(), item.IMDbId(), item.productionYear(), e);
                }
            }
        }
    }

    /**
     * Returns existing or new series to enable season traversal.
     * When a series already exists, we still need to check for new seasons/episodes.
     * <p>
     * If a series is missing critical metadata (TMDb ID, IMDb ID, production year, genres),
     * it will be recorded in the BrokenMediaItems table for later review.
     */
    private void processSeries(String folderId) {
        List<JellyfinClientItemSeriesResponseDTO> seriesList = fetchSeries(folderId);

        for (var series : seriesList) {
            MediaValidationResult validation = validateMediaItem(
                    series.providers().TMDbId(),
                    series.providers().IMDbId(),
                    series.productionYear(),
                    series.genres()
            );

            if (!validation.isValid()) {
                if (brokenMediaItemsService.findByJellyfinId(series.id()).isEmpty()) {
                    BrokenMediaItem broken = new BrokenMediaItem(
                            MediaType.SERIES,
                            series.name(),
                            series.id(),
                            series.providers().TMDbId(),
                            series.providers().IMDbId(),
                            series.productionYear(),
                            validation.missingFields()
                    );
                    brokenMediaItemsService.saveBrokenMediaItem(broken);
                }
                continue;
            }

            Set<Genre> genreSet = resolveGenres(series.genres());
            MediaItem newSeries = new MediaItem(
                    MediaType.SERIES,
                    series.name(),
                    series.id(),
                    series.providers().TMDbId(),
                    series.providers().IMDbId(),
                    series.productionYear(),
                    ValueResolver.runTimeTicksToMinutes(series.runTimeTicks()),
                    genreSet,
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptySet()
            );

            // Resolve or create: for series we always continue to check seasons
            MediaItem resolvedSeries = newSeries;

            Optional<MediaItem> seriesByJellyfinId = mediaItemsService.findByJellyfinId(series.id());
            if (seriesByJellyfinId.isPresent()) {
                resolvedSeries = seriesByJellyfinId.get();
            } else {
                // Sometimes the jellyfin ID seems to change, so we rely on tmdb present more often than imdb ( always ? ) and it is unique in our DB.
                // Update to the new jellyfin id.
                Optional<MediaItem> seriesByTMDbId = mediaItemsService.findByTMDbId(series.providers().TMDbId());
                if (seriesByTMDbId.isPresent()) {
                    resolvedSeries = seriesByTMDbId.get();

                    resolvedSeries.setJellyfinId(series.id());
                    mediaItemsService.update(
                            resolvedSeries.id(),
                            resolvedSeries
                    );
                }
            }

            if (resolvedSeries.id() == null) {
                logger.info("New series discovered: '{}'", series.name());
                resolvedSeries = mediaItemsService.save(resolvedSeries);
            } else {
                logger.debug("Series '{}' already exists, checking for new seasons/episodes", series.name());
            }

            processSeasons(series.id(), resolvedSeries);
        }
    }

    /**
     * Returns existing or new season to enable episode traversal.
     * When a season already exists, we still need to check for new episodes.
     */
    private void processSeasons(String jellyfinSeriesId, MediaItem series) {
        List<JellyfinClientItemSeasonResponseDTO> seasons = fetchSeasons(jellyfinSeriesId);

        for (var season : seasons) {
            MediaSeason newSeason = new MediaSeason(
                    series,
                    season.id(),
                    season.seasonNumber(),
                    season.name(),
                    Collections.emptySet()
            );

            MediaSeason resolvedSeason = mediaSeasonsService.findByMediaItemAndSeason(series, season.seasonNumber())
                    .orElse(newSeason);

            if (resolvedSeason.id() == null) {
                logger.info("New season {} discovered for series '{}'", season.seasonNumber(), series.title());
                resolvedSeason = mediaSeasonsService.save(resolvedSeason);
            } else {
                logger.debug("Season {} already exists for series '{}', checking for new episodes",
                        season.seasonNumber(), series.title());
            }

            processEpisodes(season.id(), resolvedSeason);
        }
    }

    private void processEpisodes(String jellyfinSeasonId, MediaSeason season) {
        List<JellyfinClientItemEpisodeResponseDTO> episodes = fetchEpisodes(jellyfinSeasonId);
        List<MediaEpisode> toSave = new ArrayList<>();

        for (var episode : episodes) {
            if (mediaEpisodesService.findByMediaSeasonAndEpisode(season, episode.episodeNumber()).isPresent()) {
                continue;
            }

            toSave.add(new MediaEpisode(
                    season,
                    episode.id(),
                    episode.episodeNumber(),
                    episode.name(),
                    ValueResolver.runTimeTicksToMinutes(episode.runTimeTicks())
            ));
        }

        if (!toSave.isEmpty()) {
            mediaEpisodesService.saveBatch(toSave);
        }
    }
    // ===== END SYNC METHODS =====

    // ===== TOP-LEVEL SYNC =====
    public void pipelineTraverseSyncMedia() {
        logger.info("Starting full traversal of Jellyfin media library");

        try {
            List<JellyfinClientItemFolderResponseDTO> folders = fetchFolders();

            for (var folder : folders) {
                processMovies(folder.id());
                processSeries(folder.id());
            }

            logger.info("Completed full traversal of Jellyfin media library");
        } catch (Exception error) {
            logger.error("Error during traversal of Jellyfin media library", error);
            throw new JellyfinTraversalExceptions.JellyfinTraversalFailedException(error);
        }
    }

    @Async("backgroundTaskExecutor")
    public void syncMediaAsync(BackgroundJob job) {
        logger.info("Starting async Jellyfin media synchronization workflow, jobId={}", job.id());
        backgroundJobService.markRunning(job);
        try {
            pipelineTraverseSyncMedia();
            mediaItemMetadataService.refreshAllMetadata(false);
            mediaItemCreditsService.updateAllMediaItemCredits(false);
            backgroundJobService.markCompleted(job, "Jellyfin sync completed successfully");
            logger.info("Async Jellyfin media synchronization workflow completed, jobId={}", job.id());
        } catch (Exception e) {
            backgroundJobService.markFailed(job, e.getMessage());
            logger.error("Async Jellyfin media synchronization workflow failed, jobId={}", job.id(), e);
        }
    }

    /**
     * Refreshes a single Jellyfin-sourced series by fetching the latest data from Jellyfin.
     * This updates seasons and episodes for the series.
     *
     * @param existingSeries The existing series to refresh (must have jellyfinId)
     * @return Refreshed series
     */
    public MediaItem refreshSeriesFromJellyfin(MediaItem existingSeries) {
        if (existingSeries.jellyfinId() == null) {
            throw new CommonExceptions.InvalidRequestExceptions(
                    "Cannot refresh non-Jellyfin series. Use TMDb refresh instead.");
        }

        if (existingSeries.mediaType() != MediaType.SERIES) {
            throw new CommonExceptions.InvalidRequestExceptions(
                    "Can only refresh series, got: " + existingSeries.mediaType());
        }

        String jellyfinId = existingSeries.jellyfinId();
        String title = existingSeries.title() != null ? existingSeries.title() : "Untitled";

        logger.info("Refreshing Jellyfin series '{}' (Jellyfin ID: {})", title, jellyfinId);

        try {
            // Process seasons and episodes
            List<JellyfinClientItemSeasonResponseDTO> seasons = fetchSeasons(jellyfinId);
            List<MediaSeason> newSeasons = new ArrayList<>();

            for (var season : seasons) {
                MediaSeason newSeason = new MediaSeason(
                        existingSeries,
                        season.id(),
                        season.seasonNumber(),
                        season.name(),
                        Collections.emptySet()
                );

                MediaSeason resolvedSeason = mediaSeasonsService.findByMediaItemAndSeason(existingSeries, season.seasonNumber())
                        .orElse(newSeason);

                if (resolvedSeason.id() == null) {
                    newSeasons.add(resolvedSeason);
                }
            }

            if (!newSeasons.isEmpty()) {
                mediaSeasonsService.saveBatch(newSeasons);
            }

            // Process episodes for each season
            for (var season : seasons) {
                MediaSeason resolvedSeason = mediaSeasonsService.findByMediaItemAndSeason(existingSeries, season.seasonNumber())
                        .orElseThrow();
                processEpisodes(season.id(), resolvedSeason);
            }

            logger.info("Successfully refreshed Jellyfin series '{}'", title);
            return existingSeries;
        } catch (Exception e) {
            logger.error("Failed to refresh Jellyfin series '{}'", title, e);
            throw e;
        }
    }
    // ===== END TOP-LEVEL SYNC =====
}
