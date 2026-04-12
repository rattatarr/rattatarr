package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.jellyfin.JellyfinClient;
import com.rattatarr.rattatarr.clients.jellyfin.requests.queries.JellyfinItemsQuery;
import com.rattatarr.rattatarr.clients.jellyfin.responses.*;
import com.rattatarr.rattatarr.clients.jellyfin.responses.wrappers.JellyfinClientItemsWrapper;
import com.rattatarr.rattatarr.clients.jellyfin.responses.wrappers.JellyfinClientProviderIdsWrapper;
import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.exceptions.JellyfinTraversalExceptions;
import com.rattatarr.rattatarr.models.JellyfinMediaType;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.BrokenMediaItem;
import com.rattatarr.rattatarr.models.entities.Genre;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaSeason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JellyfinTraversalServiceTest {

    @Mock
    private JellyfinClient jellyfinClient;
    @Mock
    private GenresService genresService;
    @Mock
    private MediaItemsService mediaItemsService;
    @Mock
    private MediaSeasonsService mediaSeasonsService;
    @Mock
    private MediaEpisodesService mediaEpisodesService;
    @Mock
    private BrokenMediaItemsService brokenMediaItemsService;

    @InjectMocks
    private JellyfinTraversalService jellyfinTraversalService;

    private JellyfinClientItemFolderResponseDTO testFolder;
    private JellyfinClientItemMovieResponseDTO testMovie;
    private JellyfinClientItemSeriesResponseDTO testSeries;
    private JellyfinClientItemSeasonResponseDTO testSeason;
    private JellyfinClientItemEpisodeResponseDTO testEpisode;
    private Genre testGenre;

    @BeforeEach
    void setUp() {
        JellyfinClientProviderIdsWrapper validProviders = new JellyfinClientProviderIdsWrapper("imdb123", "tmdb456");

        testFolder = new JellyfinClientItemFolderResponseDTO(
                "Test Folder",
                "folder-id-123",
                JellyfinMediaType.FOLDER
        );

        testMovie = new JellyfinClientItemMovieResponseDTO(
                "Test Movie",
                "movie-id-123",
                JellyfinMediaType.MOVIE,
                List.of("Action", "Drama"),
                validProviders,
                2023,
                600000000000L // 10 minutes in ticks
        );

        testSeries = new JellyfinClientItemSeriesResponseDTO(
                "Test Series",
                "series-id-123",
                JellyfinMediaType.SERIES,
                List.of("Comedy", "Drama"),
                validProviders,
                2022,
                600000000000L // 10 minutes in ticks
        );

        testSeason = new JellyfinClientItemSeasonResponseDTO(
                "Season 1",
                "season-id-123",
                JellyfinMediaType.SEASON,
                Collections.emptyList(),
                validProviders,
                2022,
                1
        );

        testEpisode = new JellyfinClientItemEpisodeResponseDTO(
                "Episode 1",
                "episode-id-123",
                JellyfinMediaType.EPISODE,
                Collections.emptyList(),
                validProviders,
                2022,
                1,
                300000000000L // 5 minutes in ticks
        );

        testGenre = new Genre("Action");
    }

    @Test
    void jellyfinTraversalService_shouldBeInstantiated() {
        // When/Then - verify service can be instantiated with all dependencies
        verify(jellyfinClient, never()).getItems(any());
    }

    @Test
    void pipelineTraverseSyncMedia_shouldCompleteSuccessfully_whenValidDataProvided() {
        // Given
        JellyfinClientItemsWrapper foldersWrapper = new JellyfinClientItemsWrapper(
                List.of(testFolder)
        );
        JellyfinClientItemsWrapper moviesWrapper = new JellyfinClientItemsWrapper(
                List.of(testMovie)
        );
        JellyfinClientItemsWrapper seriesWrapper = new JellyfinClientItemsWrapper(
                List.of(testSeries)
        );
        JellyfinClientItemsWrapper seasonsWrapper = new JellyfinClientItemsWrapper(
                List.of(testSeason)
        );
        JellyfinClientItemsWrapper episodesWrapper = new JellyfinClientItemsWrapper(
                List.of(testEpisode)
        );

        when(jellyfinClient.getItems(any(JellyfinItemsQuery.class)))
                .thenReturn(foldersWrapper)
                .thenReturn(moviesWrapper)
                .thenReturn(seriesWrapper)
                .thenReturn(seasonsWrapper)
                .thenReturn(episodesWrapper);

        when(genresService.findOrCreateByNameCached(anyString()))
                .thenReturn(testGenre);

        when(mediaItemsService.findByJellyfinId(anyString()))
                .thenReturn(Optional.empty());
        when(mediaItemsService.save(any(MediaItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mediaItemsService.saveBatch(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mediaSeasonsService.findByMediaItemAndSeason(any(MediaItem.class), anyInt()))
                .thenReturn(Optional.empty());
        when(mediaSeasonsService.save(any(MediaSeason.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(mediaEpisodesService.findByMediaSeasonAndEpisode(any(MediaSeason.class), anyInt()))
                .thenReturn(Optional.empty());

        // When
        jellyfinTraversalService.pipelineTraverseSyncMedia();

        // Then
        verify(jellyfinClient, atLeastOnce()).getItems(any(JellyfinItemsQuery.class));
        verify(mediaItemsService, atLeastOnce()).findByJellyfinId(anyString());
    }

    @Test
    void pipelineTraverseSyncMedia_shouldHandleEmptyFolders() {
        // Given
        JellyfinClientItemsWrapper emptyWrapper = new JellyfinClientItemsWrapper(
                Collections.emptyList()
        );

        when(jellyfinClient.getItems(any(JellyfinItemsQuery.class)))
                .thenReturn(emptyWrapper);

        // When
        jellyfinTraversalService.pipelineTraverseSyncMedia();

        // Then
        verify(jellyfinClient, times(1)).getItems(any(JellyfinItemsQuery.class));
    }

    @Test
    void pipelineTraverseSyncMedia_shouldHandleInvalidMovie_withMissingMetadata() {
        // Given
        JellyfinClientProviderIdsWrapper invalidProviders = new JellyfinClientProviderIdsWrapper(null, null);
        JellyfinClientItemMovieResponseDTO invalidMovie = new JellyfinClientItemMovieResponseDTO(
                "Invalid Movie",
                "invalid-movie-id",
                JellyfinMediaType.MOVIE,
                Collections.emptyList(),
                invalidProviders,
                null,
                600000000000L
        );

        JellyfinClientItemsWrapper foldersWrapper = new JellyfinClientItemsWrapper(
                List.of(testFolder)
        );
        JellyfinClientItemsWrapper moviesWrapper = new JellyfinClientItemsWrapper(
                List.of(invalidMovie)
        );

        when(jellyfinClient.getItems(any(JellyfinItemsQuery.class)))
                .thenReturn(foldersWrapper)
                .thenReturn(moviesWrapper)
                .thenReturn(new JellyfinClientItemsWrapper(Collections.emptyList()));

        when(brokenMediaItemsService.findByJellyfinId(anyString()))
                .thenReturn(Optional.empty());

        // When
        jellyfinTraversalService.pipelineTraverseSyncMedia();

        // Then
        verify(brokenMediaItemsService).saveBrokenMediaItem(any(BrokenMediaItem.class));
    }

    @Test
    void pipelineTraverseSyncMedia_shouldSkipExistingMovie() {
        // Given
        MediaItem existingMovie = new MediaItem(
                MediaType.MOVIE,
                "Test Movie",
                "movie-id-123",
                "tmdb456",
                "imdb123",
                2023,
                10,
                Set.of(testGenre),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet()
        );

        JellyfinClientItemsWrapper foldersWrapper = new JellyfinClientItemsWrapper(
                List.of(testFolder)
        );
        JellyfinClientItemsWrapper moviesWrapper = new JellyfinClientItemsWrapper(
                List.of(testMovie)
        );

        when(jellyfinClient.getItems(any(JellyfinItemsQuery.class)))
                .thenReturn(foldersWrapper)
                .thenReturn(moviesWrapper)
                .thenReturn(new JellyfinClientItemsWrapper(Collections.emptyList()));

        when(mediaItemsService.findByJellyfinId("movie-id-123"))
                .thenReturn(Optional.of(existingMovie));

        // When
        jellyfinTraversalService.pipelineTraverseSyncMedia();

        // Then
        verify(mediaItemsService).findByJellyfinId("movie-id-123");
    }

    @Test
    void pipelineTraverseSyncMedia_shouldHandleInvalidSeries_withMissingMetadata() {
        // Given
        JellyfinClientProviderIdsWrapper invalidProviders = new JellyfinClientProviderIdsWrapper(null, "tmdb456");
        JellyfinClientItemSeriesResponseDTO invalidSeries = new JellyfinClientItemSeriesResponseDTO(
                "Invalid Series",
                "invalid-series-id",
                JellyfinMediaType.SERIES,
                Collections.emptyList(),
                invalidProviders,
                null,
                600000000000L
        );

        JellyfinClientItemsWrapper foldersWrapper = new JellyfinClientItemsWrapper(
                List.of(testFolder)
        );
        JellyfinClientItemsWrapper seriesWrapper = new JellyfinClientItemsWrapper(
                List.of(invalidSeries)
        );

        when(jellyfinClient.getItems(any(JellyfinItemsQuery.class)))
                .thenReturn(foldersWrapper)
                .thenReturn(new JellyfinClientItemsWrapper(Collections.emptyList()))
                .thenReturn(seriesWrapper);

        when(brokenMediaItemsService.findByJellyfinId(anyString()))
                .thenReturn(Optional.empty());

        // When
        jellyfinTraversalService.pipelineTraverseSyncMedia();

        // Then
        verify(brokenMediaItemsService).saveBrokenMediaItem(any(BrokenMediaItem.class));
    }

    @Test
    void pipelineTraverseSyncMedia_shouldNotDuplicateBrokenMediaItem() {
        // Given
        JellyfinClientProviderIdsWrapper invalidProviders = new JellyfinClientProviderIdsWrapper(null, null);
        JellyfinClientItemMovieResponseDTO invalidMovie = new JellyfinClientItemMovieResponseDTO(
                "Invalid Movie",
                "invalid-movie-id",
                JellyfinMediaType.MOVIE,
                Collections.emptyList(),
                invalidProviders,
                null,
                600000000000L
        );

        BrokenMediaItem existingBroken = new BrokenMediaItem(
                MediaType.MOVIE,
                "Invalid Movie",
                "invalid-movie-id",
                null,
                null,
                null,
                "TMDbId, IMDbId, ProductionYear, Genres"
        );

        JellyfinClientItemsWrapper foldersWrapper = new JellyfinClientItemsWrapper(
                List.of(testFolder)
        );
        JellyfinClientItemsWrapper moviesWrapper = new JellyfinClientItemsWrapper(
                List.of(invalidMovie)
        );

        when(jellyfinClient.getItems(any(JellyfinItemsQuery.class)))
                .thenReturn(foldersWrapper)
                .thenReturn(moviesWrapper)
                .thenReturn(new JellyfinClientItemsWrapper(Collections.emptyList()));

        when(brokenMediaItemsService.findByJellyfinId("invalid-movie-id"))
                .thenReturn(Optional.of(existingBroken));

        // When
        jellyfinTraversalService.pipelineTraverseSyncMedia();

        // Then
        verify(brokenMediaItemsService, never()).saveBrokenMediaItem(any(BrokenMediaItem.class));
    }

    @Test
    void pipelineTraverseSyncMedia_shouldHandleClientError() {
        // Given
        when(jellyfinClient.getItems(any(JellyfinItemsQuery.class)))
                .thenThrow(new RuntimeException("Jellyfin connection failed"));

        // When/Then
        assertThrows(JellyfinTraversalExceptions.JellyfinTraversalFailedException.class, () ->
                jellyfinTraversalService.pipelineTraverseSyncMedia()
        );
    }

    @Test
    void pipelineTraverseSyncMedia_shouldProcessMultipleFolders() {
        // Given
        JellyfinClientItemFolderResponseDTO folder1 = new JellyfinClientItemFolderResponseDTO(
                "Movies Folder",
                "folder-1",
                JellyfinMediaType.FOLDER
        );
        JellyfinClientItemFolderResponseDTO folder2 = new JellyfinClientItemFolderResponseDTO(
                "TV Shows Folder",
                "folder-2",
                JellyfinMediaType.FOLDER
        );

        JellyfinClientItemsWrapper foldersWrapper = new JellyfinClientItemsWrapper(
                List.of(folder1, folder2)
        );
        JellyfinClientItemsWrapper emptyWrapper = new JellyfinClientItemsWrapper(
                Collections.emptyList()
        );

        when(jellyfinClient.getItems(any(JellyfinItemsQuery.class)))
                .thenReturn(foldersWrapper)
                .thenReturn(emptyWrapper)
                .thenReturn(emptyWrapper)
                .thenReturn(emptyWrapper)
                .thenReturn(emptyWrapper);

        // When
        jellyfinTraversalService.pipelineTraverseSyncMedia();

        // Then
        verify(jellyfinClient, atLeast(3)).getItems(any(JellyfinItemsQuery.class));
    }

    @Test
    void pipelineTraverseSyncMedia_shouldProcessMultipleGenres() {
        // Given
        Genre actionGenre = new Genre("Action");
        Genre dramaGenre = new Genre("Drama");

        JellyfinClientItemsWrapper foldersWrapper = new JellyfinClientItemsWrapper(
                List.of(testFolder)
        );
        JellyfinClientItemsWrapper moviesWrapper = new JellyfinClientItemsWrapper(
                List.of(testMovie)
        );

        when(jellyfinClient.getItems(any(JellyfinItemsQuery.class)))
                .thenReturn(foldersWrapper)
                .thenReturn(moviesWrapper)
                .thenReturn(new JellyfinClientItemsWrapper(Collections.emptyList()));

        when(genresService.findOrCreateByNameCached("Action"))
                .thenReturn(actionGenre);
        when(genresService.findOrCreateByNameCached("Drama"))
                .thenReturn(dramaGenre);

        when(mediaItemsService.findByJellyfinId(anyString()))
                .thenReturn(Optional.empty());
        when(mediaItemsService.saveBatch(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        jellyfinTraversalService.pipelineTraverseSyncMedia();

        // Then
        verify(genresService).findOrCreateByNameCached("Action");
        verify(genresService).findOrCreateByNameCached("Drama");
    }

    // ===== REFRESH SINGLE SERIES TESTS =====

    @Test
    void refreshSeriesFromJellyfin_shouldRefreshExistingSeries() {
        // Given
        MediaItem existingSeries = new MediaItem(
                MediaType.SERIES,
                "Test Series",
                "series-id-123",  // Has jellyfinId
                "tmdb456",
                "imdb123",
                2022,
                10,
                Set.of(testGenre),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet()
        );

        MediaSeason existingSeason = new MediaSeason(
                existingSeries,
                "season-id-123",
                1,
                "Season 1",
                Collections.emptySet()
        );

        JellyfinClientItemsWrapper seasonsWrapper = new JellyfinClientItemsWrapper(List.of(testSeason));
        JellyfinClientItemsWrapper episodesWrapper = new JellyfinClientItemsWrapper(List.of(testEpisode));

        when(jellyfinClient.getItems(any(JellyfinItemsQuery.class)))
                .thenReturn(seasonsWrapper)
                .thenReturn(episodesWrapper);

        // Season already exists — both calls to findByMediaItemAndSeason return it
        when(mediaSeasonsService.findByMediaItemAndSeason(any(), anyInt()))
                .thenReturn(Optional.of(existingSeason));

        when(mediaEpisodesService.findByMediaSeasonAndEpisode(any(), anyInt()))
                .thenReturn(Optional.empty());

        // When
        MediaItem result = jellyfinTraversalService.refreshSeriesFromJellyfin(existingSeries);

        // Then
        assertEquals(existingSeries, result);
        verify(jellyfinClient, times(2)).getItems(any(JellyfinItemsQuery.class));
    }

    @Test
    void refreshSeriesFromJellyfin_shouldProcessExistingSeasonsForNewEpisodes() {
        // Given
        MediaItem existingSeries = new MediaItem(
                MediaType.SERIES,
                "Test Series",
                "series-id-123",
                "tmdb456",
                "imdb123",
                2022,
                10,
                Set.of(testGenre),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet()
        );

        MediaSeason existingSeason = new MediaSeason(
                existingSeries,
                "season-id-123",
                1,
                "Season 1",
                Collections.emptySet()
        );

        JellyfinClientItemsWrapper seasonsWrapper = new JellyfinClientItemsWrapper(List.of(testSeason));
        JellyfinClientItemsWrapper episodesWrapper = new JellyfinClientItemsWrapper(List.of(testEpisode));

        when(jellyfinClient.getItems(any(JellyfinItemsQuery.class)))
                .thenReturn(seasonsWrapper)
                .thenReturn(episodesWrapper);

        when(mediaSeasonsService.findByMediaItemAndSeason(any(), anyInt()))
                .thenReturn(Optional.of(existingSeason));

        when(mediaEpisodesService.findByMediaSeasonAndEpisode(any(), anyInt()))
                .thenReturn(Optional.empty());

        // When
        MediaItem result = jellyfinTraversalService.refreshSeriesFromJellyfin(existingSeries);

        // Then
        assertEquals(existingSeries, result);
    }

    @Test
    void refreshSeriesFromJellyfin_shouldRejectTMDbSeries() {
        // Given
        MediaItem tmdbSeries = new MediaItem(
                MediaType.SERIES,
                "TMDb Series",
                null,  // No jellyfinId
                "tmdb456",
                "imdb123",
                2022,
                10,
                Set.of(testGenre),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet()
        );

        // When/Then
        assertThrows(CommonExceptions.InvalidRequestExceptions.class, () ->
                jellyfinTraversalService.refreshSeriesFromJellyfin(tmdbSeries)
        );

        verify(jellyfinClient, never()).getItems(any());
    }

    @Test
    void refreshSeriesFromJellyfin_shouldRejectNonSeriesMediaType() {
        // Given
        MediaItem movie = new MediaItem(
                MediaType.MOVIE,
                "Test Movie",
                "jf-123",
                "tmdb456",
                "imdb123",
                2022,
                120,
                Set.of(testGenre),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet()
        );

        // When/Then
        assertThrows(CommonExceptions.InvalidRequestExceptions.class, () ->
                jellyfinTraversalService.refreshSeriesFromJellyfin(movie)
        );

        verify(jellyfinClient, never()).getItems(any());
    }

    @Test
    void refreshSeriesFromJellyfin_shouldHandleClientError() {
        // Given
        MediaItem existingSeries = new MediaItem(
                MediaType.SERIES,
                "Test Series",
                "series-id-123",
                "tmdb456",
                "imdb123",
                2022,
                10,
                Set.of(testGenre),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet()
        );

        when(jellyfinClient.getItems(any(JellyfinItemsQuery.class)))
                .thenThrow(new RuntimeException("Jellyfin API error"));

        // When/Then
        assertThrows(RuntimeException.class, () ->
                jellyfinTraversalService.refreshSeriesFromJellyfin(existingSeries)
        );
    }
}
