package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.TMDbClient;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbFindItemResponseDTO;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbMovieFullDetailsResponseDTO;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbSeasonWithEpisodesResponseDTO;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbShowFullDetailsResponseDTO;
import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.dtos.requests.SearchFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.SearchTMDbResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.SearchGroupTMDbWrapper;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.SearchTMDbWrapper;
import com.rattatarr.rattatarr.models.entities.Genre;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemMetadata;
import com.rattatarr.rattatarr.models.entities.MediaSeason;
import com.rattatarr.rattatarr.services.helpers.MediaItemViewHelper;
import com.rattatarr.rattatarr.utils.ValueResolver;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@NullMarked
public class TMDbService {
    private static final Logger logger = LoggerFactory.getLogger(TMDbService.class);
    private static final String SEARCH_POSTER_SIZE = "w185";
    private static final int MAX_SEASONS_PER_REQUEST = 19;

    private final TMDbClient tmDbClient;
    private final MediaItemsService mediaItemsService;
    private final MediaSeasonsService mediaSeasonsService;
    private final MediaEpisodesService mediaEpisodesService;
    private final MediaItemMetadataService mediaItemMetadataService;
    private final MediaSeasonMetadataService mediaSeasonMetadataService;
    private final GenresService genresService;
    private final PeopleService peopleService;
    private final MediaItemViewHelper mediaItemViewHelper;

    public TMDbService(TMDbClient tmDbClient,
                       MediaItemsService mediaItemsService,
                       MediaSeasonsService mediaSeasonsService,
                       MediaEpisodesService mediaEpisodesService,
                       MediaItemMetadataService mediaItemMetadataService,
                       MediaSeasonMetadataService mediaSeasonMetadataService,
                       GenresService genresService,
                       PeopleService peopleService,
                       MediaItemViewHelper mediaItemViewHelper
    ) {
        this.tmDbClient = tmDbClient;
        this.mediaItemsService = mediaItemsService;
        this.mediaSeasonsService = mediaSeasonsService;
        this.mediaEpisodesService = mediaEpisodesService;
        this.mediaItemMetadataService = mediaItemMetadataService;
        this.mediaSeasonMetadataService = mediaSeasonMetadataService;
        this.genresService = genresService;
        this.peopleService = peopleService;
        this.mediaItemViewHelper = mediaItemViewHelper;
    }

    public boolean testConnection() {
        return tmDbClient.testConnection();
    }

    /**
     * Deload a TMDb search result to only include necessary fields, used for item cards.
     */
    private SearchTMDbResponseDTO sanitizeSearchResult(TMDbFindItemResponseDTO result, String posterSize) {
        return SearchTMDbResponseDTO.of(
                result.id(),
                result.title(),
                result.releaseDate(),
                mediaItemViewHelper.buildUrlFromPath(result.posterPath(), posterSize),
                null,
                null
        );
    }

    private MediaItem toMediaItemMovie(TMDbMovieFullDetailsResponseDTO dto, Set<Genre> genres) {
        if (dto.releaseDate() == null || dto.releaseDate().length() < 4) {
            logger.warn("Skipping TMDb movie id={} title='{}': missing or invalid release date '{}'",
                    dto.id(), dto.title(), dto.releaseDate());
        }
        return new MediaItem(
                MediaType.MOVIE,
                dto.title(),
                null, // jellyfinId
                dto.id().toString(),
                dto.IMDbId(),
                Integer.valueOf(dto.releaseDate().substring(0, 4)),
                dto.runtime(),
                genres,
                Set.of(), // seasons
                Set.of(), // cast
                Set.of()  // crew
        );
    }

    private MediaItem toMediaItemShow(TMDbShowFullDetailsResponseDTO dto, Set<Genre> genres) {
        return new MediaItem(
                MediaType.SERIES,
                dto.name(),
                null, // jellyfinId
                dto.id().toString(),
                dto.IMDbId(),
                Integer.valueOf(dto.firstAirDate().substring(0, 4)),
                // episodeRunTimeMinutes is a list, take the average if multiple runtimes are given;
                dto.episodeRunTimeMinutes().isEmpty() ? 0 : (int) Math.round(dto.episodeRunTimeMinutes().stream().mapToInt(Integer::intValue).average().orElse(0.0)),
                genres,
                Set.of(), // seasons
                Set.of(), // cast
                Set.of()  // crew
        );
    }

    public SearchGroupTMDbWrapper searchByName(SearchFiltersDTO filters) {
        String query = filters.query();
        String finalPosterSize = ValueResolver.valueOrDefault(filters.posterSize(), SEARCH_POSTER_SIZE);

        logger.info("Searching TMDb for query: {} with poster size: {}", query, finalPosterSize);

        var moviesDTO = tmDbClient.searchMoviesByName(query, 1);
        List<SearchTMDbResponseDTO> movies = moviesDTO != null
                ? moviesDTO.results().stream().map(result -> sanitizeSearchResult(result, finalPosterSize)).toList()
                : List.of();

        var seriesDTO = tmDbClient.searchSeriesByName(query, 1);
        List<SearchTMDbResponseDTO> series = seriesDTO != null
                ? seriesDTO.results().stream().map(result -> sanitizeSearchResult(result, finalPosterSize)).toList()
                : List.of();

        return SearchGroupTMDbWrapper.of(movies, series);
    }

    public SearchTMDbWrapper searchMoviesByName(SearchFiltersDTO filters) {
        String query = filters.query();
        String finalPosterSize = ValueResolver.valueOrDefault(filters.posterSize(), SEARCH_POSTER_SIZE);
        Integer finalPage = ValueResolver.valueOrDefault(filters.page(), 1);

        logger.info("Searching TMDb for movies with query: {} on page: {} with poster size: {}", query, finalPage, finalPosterSize);

        var dto = tmDbClient.searchMoviesByName(query, finalPage);
        if (dto == null) {
            return SearchTMDbWrapper.of(List.of(), null, null, null);
        }

        var results = dto.results().stream()
                .map(result -> sanitizeSearchResult(result, finalPosterSize))
                .toList();
        return SearchTMDbWrapper.of(results, dto.totalResults(), dto.totalPages(), dto.page());
    }

    public SearchTMDbWrapper searchSeriesByName(SearchFiltersDTO filters) {
        String query = filters.query();
        String finalPosterSize = ValueResolver.valueOrDefault(filters.posterSize(), SEARCH_POSTER_SIZE);
        Integer finalPage = ValueResolver.valueOrDefault(filters.page(), 1);

        logger.info("Searching TMDb for TV shows with query: {} on page: {} with poster size: {}", query, finalPage, finalPosterSize);

        var dto = tmDbClient.searchSeriesByName(query, finalPage);
        if (dto == null) {
            return SearchTMDbWrapper.of(List.of(), null, null, null);
        }

        var results = dto.results().stream()
                .map(result -> sanitizeSearchResult(result, finalPosterSize))
                .toList();
        return SearchTMDbWrapper.of(results, dto.totalResults(), dto.totalPages(), dto.page());
    }

    public TMDbMovieFullDetailsResponseDTO findMovieById(String id) {
        return tmDbClient.findMovieFullDetailsById(id);
    }

    public TMDbShowFullDetailsResponseDTO findShowById(String id) {
        return tmDbClient.findTVShowFullDetailsById(id);
    }

    private Map<Integer, TMDbSeasonWithEpisodesResponseDTO> fetchAllSeasonDetails(
            String tmdbId, TMDbShowFullDetailsResponseDTO showDetails) {

        Map<Integer, TMDbSeasonWithEpisodesResponseDTO> allSeasonDetails =
                new HashMap<>(showDetails.getAllSeasonDetails());

        Integer numberOfSeasons = showDetails.numberOfSeasons();
        if (numberOfSeasons == null || numberOfSeasons <= MAX_SEASONS_PER_REQUEST) {
            logger.debug("Show {} has {} seasons, all fetched in initial request", tmdbId, numberOfSeasons);
            return allSeasonDetails;
        }

        // TODO: Need to fetch additional seasons in parallel
        logger.info("Show {} has {} seasons, fetching additional seasons beyond {}",
                tmdbId, numberOfSeasons, MAX_SEASONS_PER_REQUEST);

        int currentStart = MAX_SEASONS_PER_REQUEST + 1;
        while (currentStart <= numberOfSeasons) {
            int currentEnd = Math.min(currentStart + MAX_SEASONS_PER_REQUEST - 1, numberOfSeasons);
            logger.debug("Fetching seasons {}-{} for show {}", currentStart, currentEnd, tmdbId);
            TMDbShowFullDetailsResponseDTO response = tmDbClient.findTVShowAdditionalSeasons(tmdbId, currentStart, currentEnd);
            allSeasonDetails.putAll(response.getAllSeasonDetails());
            currentStart = currentEnd + 1;
        }

        logger.info("Fetched {} total season details for show {}", allSeasonDetails.size(), tmdbId);
        return allSeasonDetails;
    }

    private void upsertEpisodesFromSeasonDetails(
            Map<Integer, TMDbSeasonWithEpisodesResponseDTO> seasonDetails, MediaItem mediaItem) {

        for (Map.Entry<Integer, TMDbSeasonWithEpisodesResponseDTO> entry : seasonDetails.entrySet()) {
            int seasonNumber = entry.getKey();
            TMDbSeasonWithEpisodesResponseDTO seasonData = entry.getValue();

            if (seasonData.episodes() == null || seasonData.episodes().isEmpty()) {
                logger.debug("No episodes found for season {} of {}", seasonNumber, mediaItem.title());
                continue;
            }

            Optional<MediaSeason> seasonOpt = mediaSeasonsService.findByMediaItemAndSeason(mediaItem, seasonNumber);
            if (seasonOpt.isEmpty()) {
                logger.warn("Season {} not found for media item {}, skipping episodes",
                        seasonNumber, mediaItem.title());
                continue;
            }

            MediaSeason mediaSeason = seasonOpt.get();
            logger.debug("Upserting {} episodes for season {} of {}",
                    seasonData.episodes().size(), seasonNumber, mediaItem.title());
            mediaEpisodesService.upsertBatchFromTMDb(seasonData.episodes(), mediaSeason);
        }
    }

    private void processTVShowDetails(TMDbShowFullDetailsResponseDTO showDetails, MediaItem mediaItem) {
        // Fetch additional seasons if show has more than 19 seasons
        Map<Integer, TMDbSeasonWithEpisodesResponseDTO> allSeasonDetails =
                fetchAllSeasonDetails(mediaItem.TMDbId(), showDetails);

        MediaItemMetadata metadata = new MediaItemMetadata(
                mediaItem,
                showDetails.overview(),
                showDetails.posterPath(),
                showDetails.backdropPath()
        );
        mediaItemMetadataService.upsert(metadata, true);

        peopleService.upsertBatchFromTMDbDTOs(showDetails.credits(), mediaItem);

        mediaSeasonsService.upsertBatchFromTMDb(showDetails.seasons(), mediaItem);

        mediaSeasonMetadataService.upsertBatchFromSeasonDetails(allSeasonDetails, mediaItem, true);

        upsertEpisodesFromSeasonDetails(allSeasonDetails, mediaItem);
    }

    @Transactional
    public MediaItem importMediaItem(String id, MediaType mediaType) {
        return switch (mediaType) {
            case MOVIE -> {
                var movieDetails = tmDbClient.findMovieFullDetailsById(id);

                Optional<MediaItem> existingMediaItemOpt = mediaItemsService.findByTMDbId(id);
                if (existingMediaItemOpt.isPresent()) {
                    logger.info("Movie with TMDb ID: {} already exists in the database. Skipping import.", id);
                    yield existingMediaItemOpt.get();
                }

                Set<Genre> genres = genresService.upsertGenresFromTMDbDTOs(movieDetails.genres());
                MediaItem mediaItem = toMediaItemMovie(movieDetails, genres);
                mediaItem = mediaItemsService.save(mediaItem);

                MediaItemMetadata metadata = new MediaItemMetadata(
                        mediaItem,
                        movieDetails.overview(),
                        movieDetails.posterPath(),
                        movieDetails.backdropPath()
                );
                mediaItemMetadataService.upsert(metadata, true);

                peopleService.upsertBatchFromTMDbDTOs(movieDetails.credits(), mediaItem);

                yield mediaItem;
            }
            case SERIES -> {
                var showDetails = tmDbClient.findTVShowFullDetailsById(id);

                Optional<MediaItem> existingMediaItemOpt = mediaItemsService.findByTMDbId(id);
                if (existingMediaItemOpt.isPresent()) {
                    logger.info("TV Show with TMDb ID: {} already exists in the database. Skipping import.", id);
                    yield existingMediaItemOpt.get();
                }

                Set<Genre> genres = genresService.upsertGenresFromTMDbDTOs(showDetails.genres());
                MediaItem mediaItem = toMediaItemShow(showDetails, genres);
                mediaItem = mediaItemsService.save(mediaItem);

                processTVShowDetails(showDetails, mediaItem);

                yield mediaItem;
            }
        };
    }

    /**
     * Refreshes an existing series from TMDb.
     * Fetches latest data and upserts new seasons/episodes while updating metadata.
     * Used for TMDb-imported series that need to be kept up-to-date.
     */
    @Transactional
    public MediaItem refreshSeriesStructure(MediaItem existingSeries) {
        if (existingSeries.mediaType() != MediaType.SERIES) {
            throw new CommonExceptions.InvalidRequestExceptions("Can only refresh series, got: " +
                    existingSeries.mediaType());
        }

        String TMDbId = existingSeries.TMDbId();
        if (TMDbId == null) {
            throw new CommonExceptions.InvalidRequestExceptions("Series missing TMDb ID");
        }

        logger.info("Refreshing series '{}' from TMDb (ID: {})", existingSeries.title(), TMDbId);

        var showDetails = tmDbClient.findTVShowFullDetailsById(TMDbId);

        Set<Genre> genres = genresService.upsertGenresFromTMDbDTOs(showDetails.genres());
        existingSeries.setGenres(genres);

        if (showDetails.episodeRunTimeMinutes() != null && !showDetails.episodeRunTimeMinutes().isEmpty()) {
            Integer avgRuntime = (int) showDetails.episodeRunTimeMinutes().stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0);
            existingSeries.setRuntimeMinutes(avgRuntime);
        }

        MediaItem savedSeries = mediaItemsService.save(existingSeries);

        processTVShowDetails(showDetails, savedSeries);

        logger.info("Successfully refreshed series '{}' with {} seasons from TMDb",
                savedSeries.title(), showDetails.numberOfSeasons());

        return savedSeries;
    }
}
