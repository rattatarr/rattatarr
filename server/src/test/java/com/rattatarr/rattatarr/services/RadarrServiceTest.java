package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.radarr.RadarrClient;
import com.rattatarr.rattatarr.clients.radarr.responses.RadarrMovieResponseDTO;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.BackgroundJob;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private RadarrClient radarrClient;

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

    @InjectMocks
    private RadarrService radarrService;

    private MediaItem movieItem;
    private UUID movieId;
    private RadarrMovieResponseDTO radarrMovieWithRatings;
    private RadarrMovieResponseDTO radarrMovieNoRatings;
    private RadarrMovieResponseDTO radarrMovieNoTmdbId;

    @BeforeEach
    void setUp() {
        movieId = UUID.randomUUID();
        movieItem = new MediaItem(
                MediaType.MOVIE, "Inception", null, "27205", "tt1375666",
                2010, 148, Set.of(), Set.of(), Set.of(), Set.of()
        );

        radarrMovieWithRatings = new RadarrMovieResponseDTO(
                1, "Inception", 2010, 27205, "tt1375666", true, true,
                new RadarrMovieResponseDTO.Ratings(
                        new RadarrMovieResponseDTO.RatingEntry(500000, 8.8, "user"),
                        new RadarrMovieResponseDTO.RatingEntry(1000, 87.0, "user")
                )
        );

        radarrMovieNoRatings = new RadarrMovieResponseDTO(
                2, "Unknown Movie", 2020, 99999, null, true, false, null
        );

        radarrMovieNoTmdbId = new RadarrMovieResponseDTO(
                3, "No TMDb", 2020, null, null, false, false, null
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
        when(radarrClient.isConfigured()).thenReturn(false);

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(mediaItemsService, never()).findById(any());
        verify(radarrClient, never()).getMovies(any());
    }

    @Test
    void enrichMovieFromRadarrIfStale_whenMovieNotFound_shouldSkip() {
        when(radarrClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.empty());

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(radarrClient, never()).getMovies(any());
        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void enrichMovieFromRadarrIfStale_whenMovieHasNoTmdbId_shouldSkip() {
        MediaItem noTmdbItem = new MediaItem(
                MediaType.MOVIE, "No TMDb Movie", null, null, null,
                2020, 90, Set.of(), Set.of(), Set.of(), Set.of()
        );
        when(radarrClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(noTmdbItem));

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(radarrClient, never()).getMovies(any());
        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void enrichMovieFromRadarrIfStale_whenRatingIsFresh_shouldSkip() {
        when(radarrClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(movieId)).thenReturn(true);

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(radarrClient, never()).getMovies(any());
        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void enrichMovieFromRadarrIfStale_whenNoRatings_shouldFetchAndUpdate() {
        when(radarrClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(movieId)).thenReturn(false);
        when(radarrClient.getMovies(27205)).thenReturn(List.of(radarrMovieWithRatings));

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(radarrClient).getMovies(27205);
        verify(mediaItemMetadataService).updateExternalRatings(
                eq(movieItem),
                eq(8.8f),
                eq(87)
        );
    }

    @Test
    void enrichMovieFromRadarrIfStale_whenRatingStale_shouldRefetch() {
        when(radarrClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(movieId)).thenReturn(false);
        when(radarrClient.getMovies(27205)).thenReturn(List.of(radarrMovieWithRatings));

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(mediaItemMetadataService).updateExternalRatings(eq(movieItem), anyFloat(), anyInt());
    }

    @Test
    void enrichMovieFromRadarrIfStale_whenRadarrReturnsEmptyList_shouldSkipUpdate() {
        when(radarrClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(movieId)).thenReturn(false);
        when(radarrClient.getMovies(27205)).thenReturn(List.of());

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void enrichMovieFromRadarrIfStale_whenRadarrMovieHasNoTmdbId_shouldSkipUpdate() {
        when(radarrClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(movieId)).thenReturn(false);
        when(radarrClient.getMovies(27205)).thenReturn(List.of(radarrMovieNoTmdbId));

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void enrichMovieFromRadarrIfStale_whenRadarrThrows_shouldNotPropagate() {
        when(radarrClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(movieId)).thenReturn(false);
        when(radarrClient.getMovies(27205)).thenThrow(new RuntimeException("Radarr unreachable"));

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(mediaItemMetadataService, never()).updateExternalRatings(any(), any(), any());
    }

    @Test
    void enrichMovieFromRadarrIfStale_withNullRatingsField_shouldUpdateWithNulls() {
        when(radarrClient.isConfigured()).thenReturn(true);
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(movieItem));
        when(mediaItemMetadataService.isRatingFresh(movieId)).thenReturn(false);
        when(radarrClient.getMovies(99999)).thenReturn(List.of(radarrMovieNoRatings));

        MediaItem itemWithOtherTmdbId = new MediaItem(
                MediaType.MOVIE, "Unknown Movie", null, "99999", null,
                2020, 90, Set.of(), Set.of(), Set.of(), Set.of()
        );
        when(mediaItemsService.findById(movieId)).thenReturn(Optional.of(itemWithOtherTmdbId));

        radarrService.enrichMovieFromRadarrIfStale(movieId);

        verify(mediaItemMetadataService).updateExternalRatings(eq(itemWithOtherTmdbId), isNull(), isNull());
    }

    // --- importAllMovies ---

    @Test
    void importAllMovies_withValidMovies_shouldImportAndUpdateRatings() {
        when(tmDbService.importMediaItem("27205", MediaType.MOVIE)).thenReturn(movieItem);

        radarrService.importAllMovies(List.of(radarrMovieWithRatings));

        verify(tmDbService).importMediaItem("27205", MediaType.MOVIE);
        verify(mediaItemMetadataService).updateExternalRatings(
                eq(movieItem),
                eq(8.8f),
                eq(87)
        );
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
        when(radarrClient.isConfigured()).thenReturn(true);
        when(radarrClient.getMovies(null)).thenReturn(List.of(radarrMovieWithRatings));
        when(tmDbService.importMediaItem("27205", MediaType.MOVIE)).thenReturn(movieItem);

        radarrService.triggerBackgroundImport(job);

        verify(backgroundJobService).markRunning(job);
        verify(backgroundJobService).markCompleted(eq(job), anyString());
        verify(backgroundJobService, never()).markFailed(any(), any());
    }

    @Test
    void triggerBackgroundImport_onFailure_shouldMarkFailed() {
        BackgroundJob job = new BackgroundJob(JobType.RADARR_IMPORT, null);
        when(radarrClient.isConfigured()).thenReturn(true);
        when(radarrClient.getMovies(null)).thenThrow(new RuntimeException("Connection refused"));

        radarrService.triggerBackgroundImport(job);

        verify(backgroundJobService).markRunning(job);
        verify(backgroundJobService).markFailed(eq(job), anyString());
        verify(backgroundJobService, never()).markCompleted(any(), any());
    }

    // --- runImport ---

    @Test
    void runImport_whenNotConfigured_shouldSkip() {
        when(radarrClient.isConfigured()).thenReturn(false);

        radarrService.runImport();

        verify(radarrClient, never()).getMovies(any());
    }

    @Test
    void runImport_whenConfigured_shouldFetchAndImport() {
        when(radarrClient.isConfigured()).thenReturn(true);
        when(radarrClient.getMovies(null)).thenReturn(List.of(radarrMovieWithRatings));
        when(tmDbService.importMediaItem("27205", MediaType.MOVIE)).thenReturn(movieItem);

        radarrService.runImport();

        verify(radarrClient).getMovies(null);
        verify(tmDbService).importMediaItem("27205", MediaType.MOVIE);
    }
}
