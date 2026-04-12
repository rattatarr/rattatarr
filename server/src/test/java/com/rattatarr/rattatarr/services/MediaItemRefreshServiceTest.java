package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MediaItemRefreshServiceTest {

    @Mock
    private MediaItemsService mediaItemsService;
    @Mock
    private TMDbService tmDbService;
    @Mock
    private JellyfinTraversalService jellyfinTraversalService;
    @Mock
    private SettingsService settingsService;

    @InjectMocks
    private MediaItemRefreshService refreshService;

    private MediaItem tmdbSeries;
    private MediaItem jellyfinSeries;
    private MediaItem movie;
    private Duration threshold;

    @BeforeEach
    void setUp() {
        threshold = Duration.ofDays(2);

        tmdbSeries = new MediaItem(
                MediaType.SERIES,
                "TMDb Series",
                null,  // No jellyfinId
                "tmdb123",
                "imdb123",
                2022,
                45,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );

        jellyfinSeries = new MediaItem(
                MediaType.SERIES,
                "Jellyfin Series",
                "jf-123",  // Has jellyfinId
                "tmdb456",
                "imdb456",
                2022,
                45,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );

        movie = new MediaItem(
                MediaType.MOVIE,
                "Test Movie",
                "jf-456",
                "tmdb789",
                "imdb789",
                2022,
                120,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );

        when(settingsService.getDurationSetting(
                eq(SettingsService.SYNC_STALE_THRESHOLD),
                any(Duration.class)
        )).thenReturn(threshold);
    }

    // ===== REFRESH ROUTING TESTS =====

    @Test
    void refresh_shouldRouteTMDbSeriesCorrectly() {
        // Given
        when(tmDbService.refreshSeriesStructure(tmdbSeries))
                .thenReturn(tmdbSeries);

        // When
        MediaItem result = refreshService.refresh(tmdbSeries);

        // Then
        assertEquals(tmdbSeries, result);
        verify(tmDbService).refreshSeriesStructure(tmdbSeries);
        verify(jellyfinTraversalService, never()).refreshSeriesFromJellyfin(any());
    }

    @Test
    void refresh_shouldRouteJellyfinSeriesCorrectly() {
        // Given
        when(jellyfinTraversalService.refreshSeriesFromJellyfin(jellyfinSeries))
                .thenReturn(jellyfinSeries);

        // When
        MediaItem result = refreshService.refresh(jellyfinSeries);

        // Then
        assertEquals(jellyfinSeries, result);
        verify(jellyfinTraversalService).refreshSeriesFromJellyfin(jellyfinSeries);
        verify(tmDbService, never()).refreshSeriesStructure(any());
    }

    @Test
    void refresh_shouldRejectNonSeriesMediaType() {
        // When/Then
        assertThrows(CommonExceptions.InvalidRequestExceptions.class, () -> refreshService.refresh(movie));

        verify(tmDbService, never()).refreshSeriesStructure(any());
        verify(jellyfinTraversalService, never()).refreshSeriesFromJellyfin(any());
    }

    // ===== REFRESH IF STALE TESTS =====

    @Test
    void refreshIfStale_shouldRefreshStaleTMDbSeries() {
        // Given - stale series
        when(mediaItemsService.isStale(tmdbSeries, threshold)).thenReturn(true);
        when(tmDbService.refreshSeriesStructure(tmdbSeries))
                .thenReturn(tmdbSeries);

        // When
        MediaItem result = refreshService.refreshIfStale(tmdbSeries);

        // Then
        assertEquals(tmdbSeries, result);
        verify(mediaItemsService).isStale(tmdbSeries, threshold);
        verify(tmDbService).refreshSeriesStructure(tmdbSeries);
    }

    @Test
    void refreshIfStale_shouldRefreshStaleJellyfinSeries() {
        // Given - stale series
        when(mediaItemsService.isStale(jellyfinSeries, threshold)).thenReturn(true);
        when(jellyfinTraversalService.refreshSeriesFromJellyfin(jellyfinSeries))
                .thenReturn(jellyfinSeries);

        // When
        MediaItem result = refreshService.refreshIfStale(jellyfinSeries);

        // Then
        assertEquals(jellyfinSeries, result);
        verify(mediaItemsService).isStale(jellyfinSeries, threshold);
        verify(jellyfinTraversalService).refreshSeriesFromJellyfin(jellyfinSeries);
    }

    @Test
    void refreshIfStale_shouldNotRefreshFreshSeries() {
        // Given - fresh series (not stale)
        when(mediaItemsService.isStale(tmdbSeries, threshold)).thenReturn(false);

        // When
        MediaItem result = refreshService.refreshIfStale(tmdbSeries);

        // Then
        assertEquals(tmdbSeries, result);
        verify(mediaItemsService).isStale(tmdbSeries, threshold);
        verify(tmDbService, never()).refreshSeriesStructure(any());
        verify(jellyfinTraversalService, never()).refreshSeriesFromJellyfin(any());
    }

    @Test
    void refreshIfStale_shouldNotRefreshMovies() {
        // When
        MediaItem result = refreshService.refreshIfStale(movie);

        // Then
        assertEquals(movie, result);
        verify(mediaItemsService, never()).isStale(any(), any());
        verify(tmDbService, never()).refreshSeriesStructure(any());
        verify(jellyfinTraversalService, never()).refreshSeriesFromJellyfin(any());
    }

    @Test
    void refreshIfStale_shouldReturnOriginalOnError() {
        // Given - stale series that will fail to refresh
        when(mediaItemsService.isStale(tmdbSeries, threshold)).thenReturn(true);
        when(tmDbService.refreshSeriesStructure(tmdbSeries))
                .thenThrow(new RuntimeException("TMDb API error"));

        // When
        MediaItem result = refreshService.refreshIfStale(tmdbSeries);

        // Then - should return original series, not error
        assertEquals(tmdbSeries, result);
        verify(tmDbService).refreshSeriesStructure(tmdbSeries);
    }

    // ===== BATCH REFRESH TESTS =====

    @Test
    void refreshAllStaleSeries_shouldRefreshMultipleSeries() {
        // Given
        MediaItem staleSeries1 = new MediaItem(
                MediaType.SERIES,
                "Stale Series 1",
                null,
                "tmdb111",
                "imdb111",
                2022,
                45,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );
        MediaItem staleSeries2 = new MediaItem(
                MediaType.SERIES,
                "Stale Series 2",
                "jf-222",
                "tmdb222",
                "imdb222",
                2022,
                45,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );

        when(mediaItemsService.findStaleSeries(threshold))
                .thenReturn(List.of(staleSeries1, staleSeries2));
        when(tmDbService.refreshSeriesStructure(staleSeries1))
                .thenReturn(staleSeries1);
        when(jellyfinTraversalService.refreshSeriesFromJellyfin(staleSeries2))
                .thenReturn(staleSeries2);

        // When
        List<MediaItem> result = refreshService.refreshAllStaleSeries();

        // Then
        assertEquals(2, result.size());
        verify(mediaItemsService).findStaleSeries(threshold);
        verify(tmDbService).refreshSeriesStructure(staleSeries1);
        verify(jellyfinTraversalService).refreshSeriesFromJellyfin(staleSeries2);
    }

    @Test
    void refreshAllStaleSeries_shouldHandleNoStaleSeries() {
        // Given
        when(mediaItemsService.findStaleSeries(threshold))
                .thenReturn(List.of());

        // When
        List<MediaItem> result = refreshService.refreshAllStaleSeries();

        // Then
        assertTrue(result.isEmpty());
        verify(mediaItemsService).findStaleSeries(threshold);
        verify(tmDbService, never()).refreshSeriesStructure(any());
        verify(jellyfinTraversalService, never()).refreshSeriesFromJellyfin(any());
    }

    @Test
    void refreshAllStaleSeries_shouldContinueOnIndividualFailure() {
        // Given
        MediaItem staleSeries1 = new MediaItem(
                MediaType.SERIES,
                "Stale Series 1",
                null,
                "tmdb111",
                "imdb111",
                2022,
                45,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );
        MediaItem staleSeries2 = new MediaItem(
                MediaType.SERIES,
                "Stale Series 2",
                null,
                "tmdb222",
                "imdb222",
                2022,
                45,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );

        when(mediaItemsService.findStaleSeries(threshold))
                .thenReturn(List.of(staleSeries1, staleSeries2));
        when(tmDbService.refreshSeriesStructure(staleSeries1))
                .thenThrow(new RuntimeException("API error"));
        when(tmDbService.refreshSeriesStructure(staleSeries2))
                .thenReturn(staleSeries2);

        // When
        List<MediaItem> result = refreshService.refreshAllStaleSeries();

        // Then - Should complete with only the successful refresh
        assertEquals(1, result.size());
        assertEquals(staleSeries2, result.get(0));
        verify(mediaItemsService).findStaleSeries(threshold);
        verify(tmDbService).refreshSeriesStructure(staleSeries1);
        verify(tmDbService).refreshSeriesStructure(staleSeries2);
    }
}
