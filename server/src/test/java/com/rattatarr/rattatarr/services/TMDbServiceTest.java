package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.TMDbClient;
import com.rattatarr.rattatarr.clients.tmdb.responses.*;
import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.dtos.requests.SearchFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.SearchGroupTMDbWrapper;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.SearchTMDbWrapper;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemMetadata;
import com.rattatarr.rattatarr.services.helpers.MediaItemViewHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TMDbServiceTest {

    @Mock
    private TMDbClient tmDbClient;
    @Mock
    private MediaItemsService mediaItemsService;
    @Mock
    private MediaSeasonsService mediaSeasonsService;
    @Mock
    private MediaEpisodesService mediaEpisodesService;
    @Mock
    private MediaItemMetadataService mediaItemMetadataService;
    @Mock
    private MediaSeasonMetadataService mediaSeasonMetadataService;
    @Mock
    private MediaItemCastService mediaItemCastService;
    @Mock
    private MediaItemCrewService mediaItemCrewService;
    @Mock
    private GenresService genresService;
    @Mock
    private PeopleService peopleService;
    @Mock
    private MediaItemViewHelper mediaItemViewHelper;

    @InjectMocks
    private TMDbService tmDbService;

    private SearchFiltersDTO searchFilters;
    private TMDbFindItemResponseDTO movieResult;
    private TMDbFindItemResponseDTO seriesResult;

    @BeforeEach
    void setUp() {
        searchFilters = new SearchFiltersDTO("test query", null, null);
        movieResult = new TMDbFindItemResponseDTO(1, "Test Movie", "Overview", "/poster.jpg", "/backdrop.jpg", "2023-01-01");
        seriesResult = new TMDbFindItemResponseDTO(2, "Test Series", "Overview", "/series.jpg", "/backdrop.jpg", "2023-01-01");
    }

    @Test
    void testConnection_shouldDelegateToClient() {
        // Given
        when(tmDbClient.testConnection()).thenReturn(true);

        // When
        boolean result = tmDbService.testConnection();

        // Then
        assertTrue(result);
        verify(tmDbClient).testConnection();
    }

    @Test
    void testConnection_shouldReturnFalseWhenClientReturnsFalse() {
        // Given
        when(tmDbClient.testConnection()).thenReturn(false);

        // When
        boolean result = tmDbService.testConnection();

        // Then
        assertFalse(result);
        verify(tmDbClient).testConnection();
    }

    @Test
    void searchByName_shouldReturnCombinedResults() {
        // Given
        TMDbSearchResponseDTO moviesResponse = new TMDbSearchResponseDTO(
                1, List.of(movieResult), 1, 1
        );
        TMDbSearchResponseDTO seriesResponse = new TMDbSearchResponseDTO(
                1, List.of(seriesResult), 1, 1
        );

        when(tmDbClient.searchMoviesByName("test query", 1)).thenReturn(moviesResponse);
        when(tmDbClient.searchSeriesByName("test query", 1)).thenReturn(seriesResponse);
        when(mediaItemViewHelper.buildUrlFromPath(anyString(), anyString())).thenReturn("http://image.url");

        // When
        SearchGroupTMDbWrapper result = tmDbService.searchByName(searchFilters);

        // Then
        assertNotNull(result);
        assertEquals(1, result.movies().size());
        assertEquals(1, result.series().size());
        verify(tmDbClient).searchMoviesByName("test query", 1);
        verify(tmDbClient).searchSeriesByName("test query", 1);
    }

    @Test
    void searchByName_shouldHandleEmptyMovieResults() {
        // Given
        TMDbSearchResponseDTO seriesResponse = new TMDbSearchResponseDTO(
                1, List.of(seriesResult), 1, 1
        );

        when(tmDbClient.searchMoviesByName("test query", 1)).thenReturn(null);
        when(tmDbClient.searchSeriesByName("test query", 1)).thenReturn(seriesResponse);
        when(mediaItemViewHelper.buildUrlFromPath(anyString(), anyString())).thenReturn("http://image.url");

        // When
        SearchGroupTMDbWrapper result = tmDbService.searchByName(searchFilters);

        // Then
        assertNotNull(result);
        assertEquals(0, result.movies().size());
        assertEquals(1, result.series().size());
    }

    @Test
    void searchMoviesByName_shouldReturnWrappedResults() {
        // Given
        TMDbSearchResponseDTO response = new TMDbSearchResponseDTO(
                1, List.of(movieResult), 10, 100
        );

        when(tmDbClient.searchMoviesByName("test query", 1)).thenReturn(response);
        when(mediaItemViewHelper.buildUrlFromPath(anyString(), anyString())).thenReturn("http://image.url");

        // When
        SearchTMDbWrapper result = tmDbService.searchMoviesByName(searchFilters);

        // Then
        assertNotNull(result);
        assertEquals(1, result.results().size());
        assertEquals(100, result.totalResults());
        assertEquals(10, result.totalPages());
        assertEquals(1, result.currentPage());
    }

    @Test
    void searchMoviesByName_shouldHandleEmptyResults() {
        // Given
        when(tmDbClient.searchMoviesByName("test query", 1)).thenReturn(null);

        // When
        SearchTMDbWrapper result = tmDbService.searchMoviesByName(searchFilters);

        // Then
        assertNotNull(result);
        assertEquals(0, result.results().size());
        assertNull(result.totalResults());
        assertNull(result.totalPages());
        assertNull(result.currentPage());
    }

    @Test
    void searchSeriesByName_shouldReturnWrappedResults() {
        // Given
        TMDbSearchResponseDTO response = new TMDbSearchResponseDTO(
                2, List.of(seriesResult), 5, 50
        );

        when(tmDbClient.searchSeriesByName("test query", 2)).thenReturn(response);
        when(mediaItemViewHelper.buildUrlFromPath(anyString(), anyString())).thenReturn("http://image.url");

        SearchFiltersDTO filters = new SearchFiltersDTO("test query", null, 2);

        // When
        SearchTMDbWrapper result = tmDbService.searchSeriesByName(filters);

        // Then
        assertNotNull(result);
        assertEquals(1, result.results().size());
        assertEquals(50, result.totalResults());
        assertEquals(5, result.totalPages());
        assertEquals(2, result.currentPage());
    }

    @Test
    void searchSeriesByName_shouldUseDefaultPageWhenNotProvided() {
        // Given
        TMDbSearchResponseDTO response = new TMDbSearchResponseDTO(
                1, List.of(seriesResult), 5, 50
        );

        when(tmDbClient.searchSeriesByName("test query", 1)).thenReturn(response);
        when(mediaItemViewHelper.buildUrlFromPath(anyString(), anyString())).thenReturn("http://image.url");

        // When
        SearchTMDbWrapper result = tmDbService.searchSeriesByName(searchFilters);

        // Then
        assertNotNull(result);
        verify(tmDbClient).searchSeriesByName("test query", 1);
    }

    @Test
    void findMovieById_shouldDelegateToClient() {
        // Given
        String movieId = "123";
        TMDbMovieFullDetailsResponseDTO movieDetails = createMockMovieDetails();

        when(tmDbClient.findMovieFullDetailsById(movieId)).thenReturn(movieDetails);

        // When
        TMDbMovieFullDetailsResponseDTO result = tmDbService.findMovieById(movieId);

        // Then
        assertNotNull(result);
        verify(tmDbClient).findMovieFullDetailsById(movieId);
    }

    @Test
    void findShowById_shouldDelegateToClient() {
        // Given
        String showId = "456";
        TMDbShowFullDetailsResponseDTO showDetails = createMockShowDetails();

        when(tmDbClient.findTVShowFullDetailsById(showId)).thenReturn(showDetails);

        // When
        TMDbShowFullDetailsResponseDTO result = tmDbService.findShowById(showId);

        // Then
        assertNotNull(result);
        verify(tmDbClient).findTVShowFullDetailsById(showId);
    }

    @Test
    void importMediaItem_movie_shouldImportSuccessfully() {
        // Given
        String movieId = "123";
        TMDbMovieFullDetailsResponseDTO movieDetails = createMockMovieDetails();
        MediaItem savedMediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt1234567",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        when(tmDbClient.findMovieFullDetailsById(movieId)).thenReturn(movieDetails);
        when(mediaItemsService.findByTMDbId(movieId)).thenReturn(Optional.empty());
        when(genresService.upsertGenresFromTMDbDTOs(anyList())).thenReturn(Set.of());
        when(mediaItemsService.save(any(MediaItem.class))).thenReturn(savedMediaItem);

        // When
        tmDbService.importMediaItem(movieId, MediaType.MOVIE);

        // Then
        verify(tmDbClient).findMovieFullDetailsById(movieId);
        verify(mediaItemsService).findByTMDbId(movieId);
        verify(genresService).upsertGenresFromTMDbDTOs(anyList());
        verify(mediaItemsService).save(any(MediaItem.class));
        verify(mediaItemMetadataService).upsert(any(MediaItemMetadata.class), eq(true));
        verify(peopleService).upsertBatchFromTMDbDTOs(any(), eq(savedMediaItem));
    }

    @Test
    void importMediaItem_movie_shouldStoreIMDbId() {
        // Given
        String movieId = "123";
        TMDbMovieFullDetailsResponseDTO movieDetails = createMockMovieDetails();

        when(tmDbClient.findMovieFullDetailsById(movieId)).thenReturn(movieDetails);
        when(mediaItemsService.findByTMDbId(movieId)).thenReturn(Optional.empty());
        when(genresService.upsertGenresFromTMDbDTOs(anyList())).thenReturn(Set.of());
        when(mediaItemsService.save(any(MediaItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        tmDbService.importMediaItem(movieId, MediaType.MOVIE);

        // Then
        verify(mediaItemsService).save(argThat(mediaItem ->
                mediaItem != null &&
                        "tt1234567".equals(mediaItem.IMDbId()) &&
                        "123".equals(mediaItem.TMDbId()) &&
                        MediaType.MOVIE.equals(mediaItem.mediaType()) &&
                        "Test Movie".equals(mediaItem.title())
        ));
    }

    @Test
    void importMediaItem_movie_shouldSkipIfAlreadyExists() {
        // Given
        String movieId = "123";
        TMDbMovieFullDetailsResponseDTO movieDetails = createMockMovieDetails();

        MediaItem existingItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt1234567",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        when(tmDbClient.findMovieFullDetailsById(movieId)).thenReturn(movieDetails);
        when(mediaItemsService.findByTMDbId(movieId)).thenReturn(Optional.of(existingItem));

        // When
        tmDbService.importMediaItem(movieId, MediaType.MOVIE);

        // Then
        verify(tmDbClient).findMovieFullDetailsById(movieId);
        verify(mediaItemsService).findByTMDbId(movieId);
        verify(mediaItemsService, never()).save(any());
    }

    @Test
    void importMediaItem_movie_shouldThrowWhenClientReturnsNull() {
        // Given
        String movieId = "123";

        when(tmDbClient.findMovieFullDetailsById(movieId)).thenReturn(null);

        // When/Then - client returning null causes NPE since code accesses .genres() directly
        assertThrows(NullPointerException.class, () -> tmDbService.importMediaItem(movieId, MediaType.MOVIE));

        verify(tmDbClient).findMovieFullDetailsById(movieId);
    }

    @Test
    void importMediaItem_movie_shouldHandleNullIMDbId() {
        // Given
        String movieId = "999";
        TMDbMovieFullDetailsResponseDTO movieDetailsWithoutIMDb = new TMDbMovieFullDetailsResponseDTO(
                999,
                "Overview",
                "/poster.jpg",
                "/backdrop.jpg",
                new TMDbCreditsResponseDTO(List.of(), List.of()),
                "homepage",
                "Movie Without IMDb",
                "Movie Without IMDb",
                "en",
                "Released",
                null, // null IMDb ID
                List.of(),
                100,
                "2023-01-01"
        );

        when(tmDbClient.findMovieFullDetailsById(movieId)).thenReturn(movieDetailsWithoutIMDb);
        when(mediaItemsService.findByTMDbId(movieId)).thenReturn(Optional.empty());
        when(genresService.upsertGenresFromTMDbDTOs(anyList())).thenReturn(Set.of());
        when(mediaItemsService.save(any(MediaItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaItem result = tmDbService.importMediaItem(movieId, MediaType.MOVIE);

        // Then
        assertNotNull(result);
        verify(mediaItemsService).save(argThat(mediaItem ->
                mediaItem != null &&
                        mediaItem.IMDbId() == null &&
                        "999".equals(mediaItem.TMDbId()) &&
                        "Movie Without IMDb".equals(mediaItem.title())
        ));
    }

    @Test
    void importMediaItem_series_shouldImportSuccessfully() {
        // Given
        String showId = "456";
        TMDbShowFullDetailsResponseDTO showDetails = createMockShowDetails();
        MediaItem savedMediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "456", "tt7654321",
                2023, null, Set.of(), Set.of(), Set.of(), Set.of()
        );

        when(tmDbClient.findTVShowFullDetailsById(showId)).thenReturn(showDetails);
        when(mediaItemsService.findByTMDbId(showId)).thenReturn(Optional.empty());
        when(genresService.upsertGenresFromTMDbDTOs(anyList())).thenReturn(Set.of());
        when(mediaItemsService.save(any(MediaItem.class))).thenReturn(savedMediaItem);

        // When
        tmDbService.importMediaItem(showId, MediaType.SERIES);

        // Then
        verify(tmDbClient).findTVShowFullDetailsById(showId);
        verify(mediaItemsService).findByTMDbId(showId);
        verify(genresService).upsertGenresFromTMDbDTOs(anyList());
        verify(mediaItemsService).save(any(MediaItem.class));
        verify(mediaItemMetadataService).upsert(any(MediaItemMetadata.class), eq(true));
        verify(peopleService).upsertBatchFromTMDbDTOs(any(), eq(savedMediaItem));
        verify(mediaSeasonsService).upsertBatchFromTMDb(anyList(), eq(savedMediaItem));
        verify(mediaSeasonMetadataService).upsertBatchFromSeasonDetails(anyMap(), eq(savedMediaItem), eq(true));
    }

    @Test
    void importMediaItem_series_shouldStoreIMDbId() {
        // Given
        String showId = "456";
        TMDbShowFullDetailsResponseDTO showDetails = createMockShowDetails();

        when(tmDbClient.findTVShowFullDetailsById(showId)).thenReturn(showDetails);
        when(mediaItemsService.findByTMDbId(showId)).thenReturn(Optional.empty());
        when(genresService.upsertGenresFromTMDbDTOs(anyList())).thenReturn(Set.of());
        when(mediaItemsService.save(any(MediaItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        tmDbService.importMediaItem(showId, MediaType.SERIES);

        // Then
        verify(mediaItemsService).save(argThat(mediaItem ->
                mediaItem != null &&
                        "tt7654321".equals(mediaItem.IMDbId()) &&
                        "456".equals(mediaItem.TMDbId()) &&
                        MediaType.SERIES.equals(mediaItem.mediaType()) &&
                        "Test Series".equals(mediaItem.title())
        ));
    }

    @Test
    void importMediaItem_series_shouldSkipIfAlreadyExists() {
        // Given
        String showId = "456";
        TMDbShowFullDetailsResponseDTO showDetails = createMockShowDetails();

        MediaItem existingShow = new MediaItem(
                MediaType.SERIES, "Test Series", null, "456", "tt7654321",
                2023, null, Set.of(), Set.of(), Set.of(), Set.of()
        );

        when(tmDbClient.findTVShowFullDetailsById(showId)).thenReturn(showDetails);
        when(mediaItemsService.findByTMDbId(showId)).thenReturn(Optional.of(existingShow));

        // When
        tmDbService.importMediaItem(showId, MediaType.SERIES);

        // Then
        verify(tmDbClient).findTVShowFullDetailsById(showId);
        verify(mediaItemsService).findByTMDbId(showId);
        verify(mediaItemsService, never()).save(any());
    }

    @Test
    void importMediaItem_series_shouldThrowWhenClientReturnsNull() {
        // Given
        String showId = "456";

        when(tmDbClient.findTVShowFullDetailsById(showId)).thenReturn(null);

        // When/Then - client returning null causes NPE since code accesses .genres() directly
        assertThrows(NullPointerException.class, () -> tmDbService.importMediaItem(showId, MediaType.SERIES));

        verify(tmDbClient).findTVShowFullDetailsById(showId);
    }

    @Test
    void importMediaItem_series_shouldHandleNullIMDbId() {
        // Given
        String showId = "888";
        Map<String, Object> seasonDetailsMap = new HashMap<>();
        Map<String, Object> season1 = new HashMap<>();
        season1.put("name", "Season 1");
        season1.put("overview", "Overview");
        season1.put("season_number", 1);
        season1.put("poster_path", "/poster.jpg");
        season1.put("episodes", List.of());
        seasonDetailsMap.put("season/1", season1);

        TMDbShowFullDetailsResponseDTO showDetailsWithoutIMDb = new TMDbShowFullDetailsResponseDTO(
                888,
                "Overview",
                "/poster.jpg",
                "/backdrop.jpg",
                List.of(new TMDbSeasonResponseDTO(10, "/poster.jpg", "2023-01-01", 1, "Season 1")),
                new TMDbCreditsResponseDTO(List.of(), List.of()),
                "homepage",
                "Series Without IMDb",
                "Series Without IMDb",
                "en",
                "Returning Series",
                null, // null IMDb ID
                List.of(),
                10,
                1,
                "2023-01-01",
                seasonDetailsMap,
                List.of(45)
        );

        when(tmDbClient.findTVShowFullDetailsById(showId)).thenReturn(showDetailsWithoutIMDb);
        when(mediaItemsService.findByTMDbId(showId)).thenReturn(Optional.empty());
        when(genresService.upsertGenresFromTMDbDTOs(anyList())).thenReturn(Set.of());
        when(mediaItemsService.save(any(MediaItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaItem result = tmDbService.importMediaItem(showId, MediaType.SERIES);

        // Then
        assertNotNull(result);
        verify(mediaItemsService).save(argThat(mediaItem ->
                mediaItem != null &&
                        mediaItem.IMDbId() == null &&
                        "888".equals(mediaItem.TMDbId()) &&
                        "Series Without IMDb".equals(mediaItem.title())
        ));
    }

    @Test
    void importMediaItem_series_shouldFetchAdditionalSeasonsWhenNeeded() {
        // Given
        String showId = "456";
        TMDbShowFullDetailsResponseDTO showDetails = createMockShowDetailsWithManySeasons(25);
        MediaItem savedMediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "456", "tt7654321",
                2023, null, Set.of(), Set.of(), Set.of(), Set.of()
        );

        TMDbShowFullDetailsResponseDTO additionalSeasons = createMockShowDetailsWithSeasons(20, 25);

        when(tmDbClient.findTVShowFullDetailsById(showId)).thenReturn(showDetails);
        when(tmDbClient.findTVShowAdditionalSeasons(eq(showId), anyInt(), anyInt()))
                .thenReturn(additionalSeasons);
        when(mediaItemsService.findByTMDbId(showId)).thenReturn(Optional.empty());
        when(genresService.upsertGenresFromTMDbDTOs(anyList())).thenReturn(Set.of());
        when(mediaItemsService.save(any(MediaItem.class))).thenReturn(savedMediaItem);

        // When
        tmDbService.importMediaItem(showId, MediaType.SERIES);

        // Then
        verify(tmDbClient).findTVShowFullDetailsById(showId);
        verify(tmDbClient, atLeastOnce()).findTVShowAdditionalSeasons(eq(showId), anyInt(), anyInt());
    }

    @Test
    void refreshSeriesStructure_shouldRefreshExistingSeries() {
        // Given
        MediaItem existingSeries = new MediaItem(
                MediaType.SERIES, "Test Series", null, "456", "tt7654321",
                2023, 45, Set.of(), Set.of(), Set.of(), Set.of()
        );
        TMDbShowFullDetailsResponseDTO showDetails = createMockShowDetails();

        when(tmDbClient.findTVShowFullDetailsById("456")).thenReturn(showDetails);
        when(genresService.upsertGenresFromTMDbDTOs(anyList())).thenReturn(Set.of());
        when(mediaItemsService.save(any(MediaItem.class))).thenReturn(existingSeries);

        // When
        MediaItem result = tmDbService.refreshSeriesStructure(existingSeries);

        // Then
        assertNotNull(result);
        verify(tmDbClient).findTVShowFullDetailsById("456");
        verify(genresService).upsertGenresFromTMDbDTOs(anyList());
        verify(mediaItemsService).save(any(MediaItem.class));
        verify(mediaItemMetadataService).upsert(any(MediaItemMetadata.class), eq(true));
        verify(peopleService).upsertBatchFromTMDbDTOs(any(), eq(existingSeries));
        verify(mediaSeasonsService).upsertBatchFromTMDb(anyList(), eq(existingSeries));
        verify(mediaSeasonMetadataService).upsertBatchFromSeasonDetails(anyMap(), eq(existingSeries), eq(true));
    }

    @Test
    void refreshSeriesStructure_shouldRejectMovieMediaType() {
        // Given
        MediaItem movie = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt1234567",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        // When/Then
        assertThrows(CommonExceptions.InvalidRequestExceptions.class, () -> tmDbService.refreshSeriesStructure(movie));

        verify(tmDbClient, never()).findTVShowFullDetailsById(anyString());
    }

    @Test
    void refreshSeriesStructure_shouldRejectSeriesWithoutTMDbId() {
        // Given
        MediaItem seriesWithoutTMDbId = new MediaItem(
                MediaType.SERIES, "Test Series", "jellyfin-id", null, "tt7654321",
                2023, null, Set.of(), Set.of(), Set.of(), Set.of()
        );

        // When/Then
        assertThrows(CommonExceptions.InvalidRequestExceptions.class, () -> tmDbService.refreshSeriesStructure(seriesWithoutTMDbId));

        verify(tmDbClient, never()).findTVShowFullDetailsById(anyString());
    }

    @Test
    void refreshSeriesStructure_shouldThrowWhenClientReturnsNull() {
        // Given
        MediaItem existingSeries = new MediaItem(
                MediaType.SERIES, "Test Series", null, "456", "tt7654321",
                2023, 45, Set.of(), Set.of(), Set.of(), Set.of()
        );

        when(tmDbClient.findTVShowFullDetailsById("456")).thenReturn(null);

        // When/Then - client returning null causes NPE since code accesses .genres() directly
        assertThrows(NullPointerException.class, () -> tmDbService.refreshSeriesStructure(existingSeries));

        verify(tmDbClient).findTVShowFullDetailsById("456");
        verify(mediaItemsService, never()).save(any());
    }

    @Test
    void refreshSeriesStructure_shouldUpdateRuntimeFromEpisodeRunTimes() {
        // Given
        MediaItem existingSeries = new MediaItem(
                MediaType.SERIES, "Test Series", null, "456", "tt7654321",
                2023, 30, Set.of(), Set.of(), Set.of(), Set.of()
        );

        TMDbShowFullDetailsResponseDTO showDetails = createMockShowDetails();

        when(tmDbClient.findTVShowFullDetailsById("456")).thenReturn(showDetails);
        when(genresService.upsertGenresFromTMDbDTOs(anyList())).thenReturn(Set.of());
        when(mediaItemsService.save(any(MediaItem.class))).thenAnswer(invocation -> {
            MediaItem saved = invocation.getArgument(0);
            assertEquals(45, saved.runtimeMinutes());
            return saved;
        });

        // When
        MediaItem result = tmDbService.refreshSeriesStructure(existingSeries);

        // Then
        assertNotNull(result);
        verify(mediaItemsService).save(any(MediaItem.class));
    }

    @Test
    void refreshSeriesStructure_shouldHandleShowsWithManySeasons() {
        // Given
        MediaItem existingSeries = new MediaItem(
                MediaType.SERIES, "Long Series", null, "789", "tt9999999",
                2020, 45, Set.of(), Set.of(), Set.of(), Set.of()
        );
        TMDbShowFullDetailsResponseDTO showDetails = createMockShowDetailsWithManySeasons(25);
        TMDbShowFullDetailsResponseDTO additionalSeasons = createMockShowDetailsWithSeasons(20, 25);

        when(tmDbClient.findTVShowFullDetailsById("789")).thenReturn(showDetails);
        when(tmDbClient.findTVShowAdditionalSeasons(eq("789"), anyInt(), anyInt()))
                .thenReturn(additionalSeasons);
        when(genresService.upsertGenresFromTMDbDTOs(anyList())).thenReturn(Set.of());
        when(mediaItemsService.save(any(MediaItem.class))).thenReturn(existingSeries);

        // When
        MediaItem result = tmDbService.refreshSeriesStructure(existingSeries);

        // Then
        assertNotNull(result);
        verify(tmDbClient).findTVShowFullDetailsById("789");
        verify(tmDbClient, atLeastOnce()).findTVShowAdditionalSeasons(eq("789"), anyInt(), anyInt());
        verify(mediaItemsService).save(any(MediaItem.class));
    }

    @Test
    void refreshSeriesStructure_shouldUpdateAllRelatedData() {
        // Given
        MediaItem existingSeries = new MediaItem(
                MediaType.SERIES, "Test Series", null, "456", "tt7654321",
                2023, 45, Set.of(), Set.of(), Set.of(), Set.of()
        );
        TMDbShowFullDetailsResponseDTO showDetails = createMockShowDetails();

        when(tmDbClient.findTVShowFullDetailsById("456")).thenReturn(showDetails);
        when(genresService.upsertGenresFromTMDbDTOs(anyList())).thenReturn(Set.of());
        when(mediaItemsService.save(any(MediaItem.class))).thenReturn(existingSeries);

        // When
        MediaItem result = tmDbService.refreshSeriesStructure(existingSeries);

        // Then
        assertNotNull(result);
        verify(genresService).upsertGenresFromTMDbDTOs(anyList());
        verify(mediaItemsService).save(any(MediaItem.class));
        verify(mediaItemMetadataService).upsert(any(MediaItemMetadata.class), eq(true));
        verify(peopleService).upsertBatchFromTMDbDTOs(any(), eq(existingSeries));
        verify(mediaSeasonsService).upsertBatchFromTMDb(anyList(), eq(existingSeries));
        verify(mediaSeasonMetadataService).upsertBatchFromSeasonDetails(anyMap(), eq(existingSeries), eq(true));
    }

    @Test
    void importMediaItem_movie_shouldPreserveExistingIMDbIdFromTMDb() {
        // Given
        String movieId = "123";
        TMDbMovieFullDetailsResponseDTO movieDetails = createMockMovieDetails();

        when(tmDbClient.findMovieFullDetailsById(movieId)).thenReturn(movieDetails);
        when(mediaItemsService.findByTMDbId(movieId)).thenReturn(Optional.empty());
        when(genresService.upsertGenresFromTMDbDTOs(anyList())).thenReturn(Set.of());
        when(mediaItemsService.save(any(MediaItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaItem result = tmDbService.importMediaItem(movieId, MediaType.MOVIE);

        // Then
        assertNotNull(result);
        assertEquals("tt1234567", result.IMDbId());
        assertEquals("123", result.TMDbId());
        assertEquals("Test Movie", result.title());
        assertEquals(MediaType.MOVIE, result.mediaType());
        assertEquals(2023, result.productionYear());
        assertEquals(120, result.runtimeMinutes());
    }

    @Test
    void importMediaItem_series_shouldPreserveExistingIMDbIdFromTMDb() {
        // Given
        String showId = "456";
        TMDbShowFullDetailsResponseDTO showDetails = createMockShowDetails();

        when(tmDbClient.findTVShowFullDetailsById(showId)).thenReturn(showDetails);
        when(mediaItemsService.findByTMDbId(showId)).thenReturn(Optional.empty());
        when(genresService.upsertGenresFromTMDbDTOs(anyList())).thenReturn(Set.of());
        when(mediaItemsService.save(any(MediaItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaItem result = tmDbService.importMediaItem(showId, MediaType.SERIES);

        // Then
        assertNotNull(result);
        assertEquals("tt7654321", result.IMDbId());
        assertEquals("456", result.TMDbId());
        assertEquals("Test Series", result.title());
        assertEquals(MediaType.SERIES, result.mediaType());
        assertEquals(2023, result.productionYear());
        assertEquals(45, result.runtimeMinutes());
    }

    // Helper methods to create mock data
    private TMDbMovieFullDetailsResponseDTO createMockMovieDetails() {
        return new TMDbMovieFullDetailsResponseDTO(
                123,
                "Overview",
                "/poster.jpg",
                "/backdrop.jpg",
                new TMDbCreditsResponseDTO(List.of(), List.of()),
                "homepage",
                "Test Movie",
                "Test Movie",
                "en",
                "Released",
                "tt1234567",
                List.of(),
                120,
                "2023-01-01"
        );
    }

    private TMDbShowFullDetailsResponseDTO createMockShowDetails() {
        Map<String, Object> seasonDetailsMap = new HashMap<>();
        Map<String, Object> season1 = new HashMap<>();
        season1.put("name", "Season 1");
        season1.put("overview", "Overview");
        season1.put("season_number", 1);
        season1.put("poster_path", "/poster.jpg");
        season1.put("episodes", List.of());
        seasonDetailsMap.put("season/1", season1);

        return new TMDbShowFullDetailsResponseDTO(
                456,
                "Overview",
                "/poster.jpg",
                "/backdrop.jpg",
                List.of(new TMDbSeasonResponseDTO(10, "/poster.jpg", "2023-01-01", 1, "Season 1")),
                new TMDbCreditsResponseDTO(List.of(), List.of()),
                "homepage",
                "Test Series",
                "Test Series",
                "en",
                "Returning Series",
                "tt7654321",
                List.of(),
                10,
                1,
                "2023-01-01",
                seasonDetailsMap,
                List.of(45)
        );
    }

    private TMDbShowFullDetailsResponseDTO createMockShowDetailsWithManySeasons(int totalSeasons) {
        Map<String, Object> seasonDetailsMap = new HashMap<>();
        List<TMDbSeasonResponseDTO> seasonSummaries = new ArrayList<>();

        for (int i = 1; i <= Math.min(totalSeasons, 19); i++) {
            Map<String, Object> seasonData = new HashMap<>();
            seasonData.put("name", "Season " + i);
            seasonData.put("overview", "Overview");
            seasonData.put("season_number", i);
            seasonData.put("poster_path", "/poster.jpg");
            seasonData.put("episodes", List.of());
            seasonDetailsMap.put("season/" + i, seasonData);

            seasonSummaries.add(new TMDbSeasonResponseDTO(10, "/poster.jpg", "2023-01-01", i, "Season " + i));
        }

        return new TMDbShowFullDetailsResponseDTO(
                456,
                "Overview",
                "/poster.jpg",
                "/backdrop.jpg",
                seasonSummaries,
                new TMDbCreditsResponseDTO(List.of(), List.of()),
                "homepage",
                "Test Series",
                "Test Series",
                "en",
                "Returning Series",
                "tt7654321",
                List.of(),
                totalSeasons * 10,
                totalSeasons,
                "2023-01-01",
                seasonDetailsMap,
                List.of(45)
        );
    }

    private TMDbShowFullDetailsResponseDTO createMockShowDetailsWithSeasons(int startSeason, int endSeason) {
        Map<String, Object> seasonDetailsMap = new HashMap<>();

        for (int i = startSeason; i <= endSeason; i++) {
            Map<String, Object> seasonData = new HashMap<>();
            seasonData.put("name", "Season " + i);
            seasonData.put("overview", "Overview");
            seasonData.put("season_number", i);
            seasonData.put("poster_path", "/poster.jpg");
            seasonData.put("episodes", List.of());
            seasonDetailsMap.put("season/" + i, seasonData);
        }

        return new TMDbShowFullDetailsResponseDTO(
                456,
                "Overview",
                "/poster.jpg",
                "/backdrop.jpg",
                List.of(),
                new TMDbCreditsResponseDTO(List.of(), List.of()),
                "homepage",
                "Test Series",
                "Test Series",
                "en",
                "Returning Series",
                "tt7654321",
                List.of(),
                endSeason * 10,
                endSeason,
                "2023-01-01",
                seasonDetailsMap,
                List.of(45)
        );
    }
}
