package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.radarr.RadarrClient;
import com.rattatarr.rattatarr.clients.radarr.responses.RadarrInternalMovieResponseDTO;
import com.rattatarr.rattatarr.clients.radarr.responses.RadarrMovieLookupResponseDTO;
import com.rattatarr.rattatarr.clients.radarr.responses.RadarrRatingEntry;
import com.rattatarr.rattatarr.clients.radarr.responses.RadarrRatings;
import com.rattatarr.rattatarr.models.ArrInstance;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.BackgroundJob;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RadarrServiceTest {

    @Mock
    private RadarrClient defaultClient;

    @Mock
    private RadarrClient animeClient;

    @Mock
    private TMDbService tmDbService;

    @Mock
    private MediaItemMetadataService mediaItemMetadataService;

    @Mock
    private MediaItemsService mediaItemsService;

    @Mock
    private BackgroundJobService backgroundJobService;

    @Mock
    private Executor tmdbApiExecutor;

    private RadarrService radarrService;

    private MediaItem movieItem;
    private UUID movieId;

    private RadarrInternalMovieResponseDTO radarrMovieWithRatings;
    private RadarrInternalMovieResponseDTO radarrMovieNoRatings;
    private RadarrInternalMovieResponseDTO radarrMovieNoTmdbId;

    private RadarrMovieLookupResponseDTO radarrLookupWithRatings;
    private RadarrMovieLookupResponseDTO radarrLookupNoRatings;
    private RadarrMovieLookupResponseDTO radarrLookupNoTmdbId;

    @BeforeEach
    void setUp() {
        radarrService = new RadarrService(
                defaultClient, animeClient,
                tmDbService, mediaItemMetadataService, mediaItemsService,
                backgroundJobService, tmdbApiExecutor
        );

        movieId = UUID.randomUUID();
        movieItem = new MediaItem(
                MediaType.MOVIE, "Inception", null, "27205", "tt1375666",
                2010, 148, Set.of(), Set.of(), Set.of(), Set.of()
        );
        ReflectionTestUtils.setField(movieItem, "id", movieId);

        var ratingsWithValues = new RadarrRatings(
                new RadarrRatingEntry(500000, 8.8, "user"),
                new RadarrRatingEntry(1000, 87.0, "user")
        );

        radarrMovieWithRatings = new RadarrInternalMovieResponseDTO(
                1, "Inception", 2010, 27205, "tt1375666", true, true, ratingsWithValues
        );
        radarrMovieNoRatings = new RadarrInternalMovieResponseDTO(
                2, "Unknown Movie", 2020, 99999, null, true, false, null
        );
        radarrMovieNoTmdbId = new RadarrInternalMovieResponseDTO(
                3, "No TMDb", 2020, null, null, false, false, null
        );

        radarrLookupWithRatings = new RadarrMovieLookupResponseDTO(
                "Inception", 2010, 27205, "tt1375666", true, ratingsWithValues
        );
        radarrLookupNoRatings = new RadarrMovieLookupResponseDTO(
                "Unknown Movie", 2020, 99999, null, true, null
        );
        radarrLookupNoTmdbId = new RadarrMovieLookupResponseDTO(
                "No TMDb", 2020, null, null, false, null
        );

        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(tmdbApiExecutor).execute(any());
    }

    // --- enrichMovieFromRadarrIfStale ---

    @Test
    void enrichMovieFromRadarrIfStale_whenRadarrNotConfigured_shouldSkip() {
        when(defaultClient.isConfigured()).thenReturn(false);

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(mediaItemsService, never()).findById(any());
    }

    @Test
    void enrichMovieFromRadarrIfStale_whenMovieNotFound_shouldSkip() {
        when(defaultClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.empty());

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void enrichMovieFromRadarrIfStale_whenMovieHasNoTmdbId_shouldSkip() {
        MediaItem noTmdbItem = new MediaItem(
                MediaType.MOVIE, "No TMDb Movie", null, null, null,
                2020, 90, Set.of(), Set.of(), Set.of(), Set.of()
        );
        when(defaultClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(noTmdbItem));

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void enrichMovieFromRadarrIfStale_whenRatingIsFresh_shouldSkip() {
        when(defaultClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(movieId)).thenReturn(true);

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void enrichMovieFromRadarrIfStale_whenNoRatings_shouldFetchAndUpdate() {
        when(defaultClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(movieId)).thenReturn(false);
        when(defaultClient.lookupByTmdbId(27205)).thenReturn(radarrLookupWithRatings);

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(defaultClient).lookupByTmdbId(27205);
        verify(mediaItemMetadataService).updateExternalRatings(eq(movieItem), eq(8.8f), eq(87));
    }

    @Test
    void enrichMovieFromRadarrIfStale_whenRatingStale_shouldRefetch() {
        when(defaultClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(movieId)).thenReturn(false);
        when(defaultClient.lookupByTmdbId(27205)).thenReturn(radarrLookupWithRatings);

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(mediaItemMetadataService).updateExternalRatings(eq(movieItem), anyFloat(), anyInt());
    }

    @Test
    void enrichMovieFromRadarrIfStale_whenRadarrMovieNotFound_shouldSkipUpdate() {
        when(defaultClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(movieId)).thenReturn(false);
        when(defaultClient.lookupByTmdbId(27205)).thenThrow(new RuntimeException("404 Not Found"));

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void enrichMovieFromRadarrIfStale_whenRadarrMovieHasNoTmdbId_shouldSkipUpdate() {
        when(defaultClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(movieId)).thenReturn(false);
        when(defaultClient.lookupByTmdbId(27205)).thenReturn(radarrLookupNoTmdbId);

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void enrichMovieFromRadarrIfStale_whenRadarrThrows_shouldNotPropagate() {
        when(defaultClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(movieId)).thenReturn(false);
        when(defaultClient.lookupByTmdbId(27205)).thenThrow(new RuntimeException("Radarr unreachable"));

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void enrichMovieFromRadarrIfStale_withNullRatingsField_shouldUpdateWithNulls() {
        MediaItem itemWithOtherTmdbId = new MediaItem(
                MediaType.MOVIE, "Unknown Movie", null, "99999", null,
                2020, 90, Set.of(), Set.of(), Set.of(), Set.of()
        );
        when(defaultClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(itemWithOtherTmdbId));
        when(mediaItemMetadataService.isRatingFresh(movieId)).thenReturn(false);
        when(defaultClient.lookupByTmdbId(99999)).thenReturn(radarrLookupNoRatings);

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(mediaItemMetadataService).updateExternalRatings(eq(itemWithOtherTmdbId), isNull(), isNull());
    }

    // --- importAllMovies ---

    @Test
    void importAllMovies_withValidMovies_shouldImportAndUpdateRatings() {
        when(tmDbService.importMediaItem("27205", MediaType.MOVIE)).thenReturn(movieItem);

        radarrService.importAllMovies(List.of(radarrMovieWithRatings));

        verify(tmDbService).importMediaItem("27205", MediaType.MOVIE);
        verify(mediaItemMetadataService).updateExternalRatings(eq(movieItem), eq(8.8f), eq(87));
    }

    @Test
    void importAllMovies_withMovieMissingTmdbId_shouldSkip() {
        radarrService.importAllMovies(List.of(radarrMovieNoTmdbId));

        verify(tmDbService, never()).importMediaItem(anyString(), any());
        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void importAllMovies_withEmptyList_shouldNotProcess() {
        radarrService.importAllMovies(List.of());

        verify(tmDbService, never()).importMediaItem(anyString(), any());
        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void importAllMovies_withNullRatings_shouldUpdateWithNulls() {
        when(tmDbService.importMediaItem("99999", MediaType.MOVIE)).thenReturn(movieItem);

        radarrService.importAllMovies(List.of(radarrMovieNoRatings));

        verify(mediaItemMetadataService).updateExternalRatings(eq(movieItem), isNull(), isNull());
    }

    // --- triggerBackgroundImport ---

    @Test
    void triggerBackgroundImport_onSuccess_shouldMarkCompleted() {
        BackgroundJob job = new BackgroundJob(JobType.RADARR_IMPORT, null);
        ReflectionTestUtils.setField(job, "id", UUID.randomUUID());
        when(defaultClient.isConfigured()).thenReturn(true);
        when(defaultClient.getMonitoredInternalMovies(null)).thenReturn(List.of(radarrMovieWithRatings));
        when(tmDbService.importMediaItem("27205", MediaType.MOVIE)).thenReturn(movieItem);

        radarrService.triggerBackgroundImport(job, ArrInstance.DEFAULT);

        verify(backgroundJobService).markRunning(job);
        verify(backgroundJobService).markCompleted(eq(job), anyString());
        verify(backgroundJobService, never()).markFailed(any(), any());
    }

    @Test
    void triggerBackgroundImport_onFailure_shouldMarkFailed() {
        BackgroundJob job = new BackgroundJob(JobType.RADARR_IMPORT, null);
        ReflectionTestUtils.setField(job, "id", UUID.randomUUID());
        when(defaultClient.isConfigured()).thenReturn(true);
        when(defaultClient.getMonitoredInternalMovies(null)).thenThrow(new RuntimeException("Connection refused"));

        radarrService.triggerBackgroundImport(job, ArrInstance.DEFAULT);

        verify(backgroundJobService).markRunning(job);
        verify(backgroundJobService).markFailed(eq(job), anyString());
        verify(backgroundJobService, never()).markCompleted(any(), any());
    }

    // --- runImport ---

    @Test
    void runImport_whenNotConfigured_shouldSkip() {
        when(defaultClient.isConfigured()).thenReturn(false);

        radarrService.runImport(ArrInstance.DEFAULT);

        verify(defaultClient, never()).getMonitoredInternalMovies(any());
    }

    @Test
    void runImport_whenConfigured_shouldFetchAndImport() {
        when(defaultClient.isConfigured()).thenReturn(true);
        when(defaultClient.getMonitoredInternalMovies(null)).thenReturn(List.of(radarrMovieWithRatings));
        when(tmDbService.importMediaItem("27205", MediaType.MOVIE)).thenReturn(movieItem);

        radarrService.runImport(ArrInstance.DEFAULT);

        verify(defaultClient).getMonitoredInternalMovies(null);
        verify(tmDbService).importMediaItem("27205", MediaType.MOVIE);
    }

    // --- runRatingsRefresh ---

    @Test
    void runRatingsRefresh_whenNotConfigured_shouldSkip() {
        when(defaultClient.isConfigured()).thenReturn(false);

        radarrService.runRatingsRefresh(ArrInstance.DEFAULT);

        verify(mediaItemsService, never()).findAllMoviesWithTmdbId();
        verify(defaultClient, never()).lookupByTmdbId(anyInt());
    }

    @Test
    void runRatingsRefresh_whenConfigured_shouldEnrichAllMovies() {
        when(defaultClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findAllMoviesWithTmdbId()).thenReturn(List.of(movieItem));
        when(defaultClient.lookupByTmdbId(27205)).thenReturn(radarrLookupWithRatings);

        radarrService.runRatingsRefresh(ArrInstance.DEFAULT);

        verify(mediaItemsService).findAllMoviesWithTmdbId();
        verify(defaultClient).lookupByTmdbId(27205);
        verify(mediaItemMetadataService).updateExternalRatings(eq(movieItem), anyFloat(), anyInt());
    }

    // --- enrichAllMoviesWithRadarrRatings ---

    @Test
    void enrichAllMoviesWithRadarrRatings_withFreshRating_shouldSkipLookup() {
        when(mediaItemsService.findAllMoviesWithTmdbId()).thenReturn(List.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(any())).thenReturn(true);

        radarrService.enrichAllMoviesWithRadarrRatings(ArrInstance.DEFAULT);

        verify(defaultClient, never()).lookupByTmdbId(anyInt());
        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void enrichAllMoviesWithRadarrRatings_withStaleRating_shouldLookupAndApply() {
        when(mediaItemsService.findAllMoviesWithTmdbId()).thenReturn(List.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(any())).thenReturn(false);
        when(defaultClient.lookupByTmdbId(27205)).thenReturn(radarrLookupWithRatings);

        radarrService.enrichAllMoviesWithRadarrRatings(ArrInstance.DEFAULT);

        verify(defaultClient).lookupByTmdbId(27205);
        verify(mediaItemMetadataService).updateExternalRatings(eq(movieItem), eq(8.8f), eq(87));
    }

    @Test
    void enrichAllMoviesWithRadarrRatings_whenLookupFails_shouldSkipAndContinue() {
        when(mediaItemsService.findAllMoviesWithTmdbId()).thenReturn(List.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(any())).thenReturn(false);
        when(defaultClient.lookupByTmdbId(27205)).thenThrow(new RuntimeException("Radarr unreachable"));

        radarrService.enrichAllMoviesWithRadarrRatings(ArrInstance.DEFAULT);

        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void enrichAllMoviesWithRadarrRatings_whenLookupReturnsNoTmdbId_shouldSkip() {
        when(mediaItemsService.findAllMoviesWithTmdbId()).thenReturn(List.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(any())).thenReturn(false);
        when(defaultClient.lookupByTmdbId(27205)).thenReturn(radarrLookupNoTmdbId);

        radarrService.enrichAllMoviesWithRadarrRatings(ArrInstance.DEFAULT);

        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void enrichAllMoviesWithRadarrRatings_withEmptyMovieList_shouldNotProcess() {
        when(mediaItemsService.findAllMoviesWithTmdbId()).thenReturn(List.of());

        radarrService.enrichAllMoviesWithRadarrRatings(ArrInstance.DEFAULT);

        verify(defaultClient, never()).lookupByTmdbId(anyInt());
        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    // --- triggerBackgroundRatingsRefresh ---

    @Test
    void triggerBackgroundRatingsRefresh_onSuccess_shouldMarkCompleted() {
        BackgroundJob job = new BackgroundJob(JobType.RADARR_RATINGS_REFRESH, null);
        ReflectionTestUtils.setField(job, "id", UUID.randomUUID());
        when(defaultClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findAllMoviesWithTmdbId()).thenReturn(List.of());

        radarrService.triggerBackgroundRatingsRefresh(job, ArrInstance.DEFAULT);

        verify(backgroundJobService).markRunning(job);
        verify(backgroundJobService).markCompleted(eq(job), anyString());
        verify(backgroundJobService, never()).markFailed(any(), any());
    }

    @Test
    void triggerBackgroundRatingsRefresh_onFailure_shouldMarkFailed() {
        BackgroundJob job = new BackgroundJob(JobType.RADARR_RATINGS_REFRESH, null);
        ReflectionTestUtils.setField(job, "id", UUID.randomUUID());
        when(defaultClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findAllMoviesWithTmdbId()).thenThrow(new RuntimeException("DB error"));

        radarrService.triggerBackgroundRatingsRefresh(job, ArrInstance.DEFAULT);

        verify(backgroundJobService).markRunning(job);
        verify(backgroundJobService).markFailed(eq(job), anyString());
        verify(backgroundJobService, never()).markCompleted(any(), any());
    }
}
