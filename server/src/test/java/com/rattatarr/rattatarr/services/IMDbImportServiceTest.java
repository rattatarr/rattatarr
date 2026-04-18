package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.TMDbClient;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbFindItemResponseDTO;
import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.IMDbWatchlistRow;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.BackgroundJob;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.utils.CSVProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class IMDbImportServiceTest {

    @Mock
    private TMDbClient tmdbClient;

    @Mock
    private TMDbService tmDbService;

    @Mock
    private ProfilesService profilesService;

    @Mock
    private MediaItemRatingsService mediaItemRatingsService;

    @Mock
    private BackgroundJobService backgroundJobService;

    @Mock
    private Executor tmdbApiExecutor;

    @InjectMocks
    private IMDbImportService imdbImportService;

    private Profile testProfile;
    private MediaItem testMovie;
    private MediaItem testSeries;
    private TMDbFindItemResponseDTO movieFindResponse;
    private TMDbFindItemResponseDTO seriesFindResponse;

    @BeforeEach
    void setUp() {
        testProfile = new Profile("Test User", null);
        testMovie = new MediaItem(
                MediaType.MOVIE, "The Dark Knight", null, "155", "tt0468569",
                2008, 152, Set.of(), Set.of(), Set.of(), Set.of()
        );
        testSeries = new MediaItem(
                MediaType.SERIES, "Breaking Bad", null, "1396", "tt0903747",
                2008, 47, Set.of(), Set.of(), Set.of(), Set.of()
        );
        movieFindResponse = new TMDbFindItemResponseDTO(
                155, "The Dark Knight", "Batman faces the Joker", "/poster.jpg", "/backdrop.jpg", "2008-07-18"
        );
        seriesFindResponse = new TMDbFindItemResponseDTO(
                1396, "Breaking Bad", "A chemistry teacher turns to crime", "/poster.jpg", "/backdrop.jpg", "2008-01-20"
        );

        // Mock executor to run tasks synchronously in tests (lenient to avoid UnnecessaryStubbingException)
        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(tmdbApiExecutor).execute(any());
    }

    @Test
    void importRatings_withValidRows_shouldProcessAllRows() {
        // Given
        List<IMDbWatchlistRow> rows = List.of(
                new IMDbWatchlistRow("tt0468569", 9.0f, "movie", Instant.now(), MediaType.MOVIE),
                new IMDbWatchlistRow("tt0903747", 10.0f, "tvSeries", Instant.now(), MediaType.SERIES)
        );

        when(tmdbClient.findMovieByIMDbId("tt0468569")).thenReturn(movieFindResponse);
        when(tmdbClient.findTVShowByIMDbId("tt0903747")).thenReturn(seriesFindResponse);
        when(tmDbService.importMediaItem("155", MediaType.MOVIE)).thenReturn(testMovie);
        when(tmDbService.importMediaItem("1396", MediaType.SERIES)).thenReturn(testSeries);

        // When
        imdbImportService.importRatings(rows, testProfile);

        // Then
        verify(tmdbClient).findMovieByIMDbId("tt0468569");
        verify(tmdbClient).findTVShowByIMDbId("tt0903747");
        verify(tmDbService).importMediaItem("155", MediaType.MOVIE);
        verify(tmDbService).importMediaItem("1396", MediaType.SERIES);
        verify(mediaItemRatingsService, times(2)).upsert(any(), any(), anyFloat(), any());
    }

    @Test
    void importRatings_withEmptyList_shouldCompleteWithoutProcessing() {
        // Given
        List<IMDbWatchlistRow> emptyRows = List.of();

        // When
        imdbImportService.importRatings(emptyRows, testProfile);

        // Then
        verify(tmdbClient, never()).findMovieByIMDbId(anyString());
        verify(tmdbClient, never()).findTVShowByIMDbId(anyString());
        verify(tmDbService, never()).importMediaItem(anyString(), any(MediaType.class));
        verify(mediaItemRatingsService, never()).upsert(any(), any(), anyFloat(), any());
    }

    @Test
    void importRatings_withMixedSuccessAndFailures_shouldContinueProcessing() {
        // Given
        List<IMDbWatchlistRow> rows = List.of(
                new IMDbWatchlistRow("tt0468569", 9.0f, "movie", Instant.now(), MediaType.MOVIE),
                new IMDbWatchlistRow("tt9999999", 8.0f, "movie", Instant.now(), MediaType.MOVIE),
                new IMDbWatchlistRow("tt0903747", 10.0f, "tvSeries", Instant.now(), MediaType.SERIES)
        );

        when(tmdbClient.findMovieByIMDbId("tt0468569")).thenReturn(movieFindResponse);
        when(tmdbClient.findMovieByIMDbId("tt9999999"))
                .thenThrow(new RuntimeException("Not found"));
        when(tmdbClient.findTVShowByIMDbId("tt0903747")).thenReturn(seriesFindResponse);
        when(tmDbService.importMediaItem("155", MediaType.MOVIE)).thenReturn(testMovie);
        when(tmDbService.importMediaItem("1396", MediaType.SERIES)).thenReturn(testSeries);

        // When
        imdbImportService.importRatings(rows, testProfile);

        // Then
        verify(tmdbClient).findMovieByIMDbId("tt0468569");
        verify(tmdbClient).findMovieByIMDbId("tt9999999");
        verify(tmdbClient).findTVShowByIMDbId("tt0903747");
        verify(tmDbService).importMediaItem("155", MediaType.MOVIE);
        verify(tmDbService).importMediaItem("1396", MediaType.SERIES);
        verify(mediaItemRatingsService, times(2)).upsert(any(), any(), anyFloat(), any());
    }

    @Test
    void triggerBackgroundImportRatings_withValidProfile_shouldStartImport() {
        // Given
        UUID profileId = UUID.randomUUID();
        List<IMDbWatchlistRow> rows = List.of(
                new IMDbWatchlistRow("tt0468569", 9.0f, "movie", Instant.now(), MediaType.MOVIE)
        );
        BackgroundJob job = new BackgroundJob(JobType.CSV_IMPORT, profileId);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(testProfile);

        // When
        imdbImportService.triggerBackgroundImportRatings(rows, profileId, job);

        // Then
        verify(profilesService).findByIdOrThrow(eq(profileId), any());
    }

    @Test
    void triggerBackgroundImportRatings_withInvalidProfile_shouldThrowException() {
        // Given
        UUID profileId = UUID.randomUUID();
        List<IMDbWatchlistRow> rows = List.of();
        BackgroundJob job = new BackgroundJob(JobType.CSV_IMPORT, profileId);

        when(profilesService.findByIdOrThrow(eq(profileId), any()))
                .thenThrow(new ProfilesExceptions.ProfileNotFoundExceptions(profileId));

        // When/Then
        assertThrows(ProfilesExceptions.ProfileNotFoundExceptions.class, () -> {
            imdbImportService.triggerBackgroundImportRatings(rows, profileId, job);
        });

        verify(profilesService).findByIdOrThrow(eq(profileId), any());
    }

    @Test
    void parseCSV_withValidFile_shouldReturnRows() {
        // Given
        MultipartFile mockFile = mock(MultipartFile.class);
        List<IMDbWatchlistRow> expectedRows = List.of(
                new IMDbWatchlistRow("tt0468569", 9.0f, "movie", Instant.now(), MediaType.MOVIE)
        );

        try (MockedStatic<CSVProcessor> csvProcessorMock = mockStatic(CSVProcessor.class)) {
            csvProcessorMock.when(() -> CSVProcessor.parseCSV(eq(mockFile), any()))
                    .thenReturn(expectedRows);

            // When
            List<IMDbWatchlistRow> result = imdbImportService.parseCSV(mockFile);

            // Then
            assertEquals(expectedRows, result);
        }
    }

}
