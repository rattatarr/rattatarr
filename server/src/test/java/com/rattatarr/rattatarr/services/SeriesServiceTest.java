package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.dtos.requests.SeriesFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.ShowResponseDTO;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.repositories.MediaItemsRepository;
import com.rattatarr.rattatarr.services.helpers.MediaItemViewHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SeriesServiceTest {

    @Mock
    private MediaItemsRepository repository;

    @Mock
    private MediaItemViewHelper mediaItemViewHelper;

    @Mock
    private MediaItemRefreshService mediaItemRefreshService;

    @Mock
    private SettingsService settingsService;

    @Mock
    private MediaItemRatingsService mediaItemRatingsService;

    @InjectMocks
    private SeriesService service;

    private MediaItem testSeries;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testSeries = new MediaItem(
                MediaType.SERIES,
                "Test Series",
                "jf-789",
                "tmdb-789",
                "imdb-789",
                2024,
                45,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );

        pageable = PageRequest.of(0, 20);

        // Mock auto-refresh config to be disabled by default
        when(settingsService.getBooleanSetting(
                eq(SettingsService.SYNC_AUTO_REFRESH_ON_READ),
                eq(false)
        )).thenReturn(false);
    }

    @Test
    void testFilterSeries_AllFilters() {
        // Given
        UUID seriesId = UUID.randomUUID();
        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                seriesId,
                "Test",
                2023,
                2024,
                Set.of("Drama"),
                false,
                false,
                "w500",
                "w1280",
                "w185",
                null,
                null,
                null,
                null,
                null
        );

        Page<MediaItem> page = new PageImpl<>(List.of(testSeries), pageable, 1);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new java.util.HashMap<>());

        // When
        Page<ShowResponseDTO> result = service.filterSeries(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAll(ArgumentMatchers.<Specification<MediaItem>>any(), eq(pageable));
        verify(mediaItemViewHelper).initializeCredits(eq(testSeries), eq(true));
        verify(mediaItemViewHelper).applyImageUrls(eq(testSeries), eq("w500"), eq("w1280"), eq("w185"), eq(false), eq(true));
        verify(mediaItemRatingsService).batchFetchRatingsMap(any(), nullable(UUID.class));
    }

    @Test
    void testFilterSeries_WithSeasons() {
        // Given
        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                null, null, null, null, null, true, false, null, null, null, null, null, null, null, null
        );

        Page<MediaItem> page = new PageImpl<>(List.of(testSeries), pageable, 1);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new java.util.HashMap<>());

        // When
        Page<ShowResponseDTO> result = service.filterSeries(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(mediaItemViewHelper).initializeCredits(eq(testSeries), eq(false));
        verify(mediaItemViewHelper).applyImageUrls(eq(testSeries), any(), any(), any(), eq(true), eq(false));
    }

    @Test
    void testFilterSeries_WithEpisodes() {
        // Given
        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                null, null, null, null, null, false, true, null, null, null, null, null, null, null, null
        );

        Page<MediaItem> page = new PageImpl<>(List.of(testSeries), pageable, 1);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new java.util.HashMap<>());

        // When
        Page<ShowResponseDTO> result = service.filterSeries(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        // withEpisodes implies withSeasons
        verify(mediaItemViewHelper).applyImageUrls(eq(testSeries), any(), any(), any(), eq(true), eq(false));
    }

    @Test
    void testFilterSeries_NoIdFilter() {
        // Given
        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                null, "Test", null, null, null, false, false, "w500", "w1280", "w185", null, null, null, null, null
        );

        Page<MediaItem> page = new PageImpl<>(List.of(testSeries), pageable, 1);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new java.util.HashMap<>());

        // When
        Page<ShowResponseDTO> result = service.filterSeries(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(mediaItemViewHelper).initializeCredits(eq(testSeries), eq(false));
        verify(mediaItemViewHelper).applyImageUrls(eq(testSeries), eq("w500"), eq("w1280"), eq("w185"), eq(false), eq(false));
    }

    @Test
    void testFilterSeries_EmptyResult() {
        // Given
        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );

        Page<MediaItem> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(emptyPage);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new java.util.HashMap<>());

        // When
        Page<ShowResponseDTO> result = service.filterSeries(filters, pageable);

        // Then
        assertTrue(result.isEmpty());
        verify(repository).findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class));
        verify(mediaItemViewHelper, never()).initializeCredits(any(), anyBoolean());
        verify(mediaItemViewHelper, never()).applyImageUrls(any(), any(), any(), any(), anyBoolean(), anyBoolean());
    }

    @Test
    void testFilterSeries_MultipleResults() {
        // Given
        MediaItem series2 = new MediaItem(
                MediaType.SERIES,
                "Another Series",
                "jf-012",
                "tmdb-012",
                "imdb-012",
                2024,
                50,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );

        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null
        );

        Page<MediaItem> page = new PageImpl<>(List.of(testSeries, series2), pageable, 2);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new java.util.HashMap<>());

        // When
        Page<ShowResponseDTO> result = service.filterSeries(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(mediaItemViewHelper, times(2)).initializeCredits(any(MediaItem.class), eq(false));
        verify(mediaItemViewHelper, times(2)).applyImageUrls(any(MediaItem.class), any(), any(), any(), eq(false), eq(false));
    }

    @Test
    void testFilterSeries_WithRatingSortAndProfileId_repositoryReceivesUnsortedPageable() {
        // Given
        UUID profileId = UUID.randomUUID();
        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                null, null, null, null, null, false, false, null, null, null, profileId, null, null, null, null
        );
        Pageable sortedByRating = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "ratings"));

        Page<MediaItem> page = new PageImpl<>(List.of(), sortedByRating, 0);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), any())).thenReturn(new java.util.HashMap<>());

        // When
        service.filterSeries(filters, sortedByRating);

        // Then
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(ArgumentMatchers.<Specification<MediaItem>>any(), pageableCaptor.capture());
        assertFalse(pageableCaptor.getValue().getSort().isSorted());
    }

    @Test
    void testFilterSeries_WithRatingSortNoProfileId_repositoryReceivesPageableWithoutRatingsSort() {
        // Given
        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                null, null, null, null, null, false, false, null, null, null, null, null, null, null, null
        );
        Pageable sortedByRating = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "ratings"));

        Page<MediaItem> page = new PageImpl<>(List.of(), sortedByRating, 0);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), any())).thenReturn(new java.util.HashMap<>());

        // When
        service.filterSeries(filters, sortedByRating);

        // Then
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(ArgumentMatchers.<Specification<MediaItem>>any(), pageableCaptor.capture());
        assertFalse(pageableCaptor.getValue().getSort().stream()
                .anyMatch(o -> o.getProperty().equalsIgnoreCase("ratings")));
    }

    @Test
    void testFilterSeries_WithRatingSortAndOtherSorts_repositoryReceivesUnsortedPageable() {
        // Given
        UUID profileId = UUID.randomUUID();
        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                null, null, null, null, null, false, false, null, null, null, profileId, null, null, null, null
        );
        Pageable mixed = PageRequest.of(0, 20, Sort.by(
                Sort.Order.asc("ratings"),
                Sort.Order.desc("productionYear")
        ));

        Page<MediaItem> page = new PageImpl<>(List.of(), mixed, 0);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), any())).thenReturn(new java.util.HashMap<>());

        // When
        service.filterSeries(filters, mixed);

        // Then
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(ArgumentMatchers.<Specification<MediaItem>>any(), pageableCaptor.capture());
        assertFalse(pageableCaptor.getValue().getSort().isSorted());
    }

    @Test
    void testFilterSeries_WithNonRatingSort_repositoryReceivesOriginalPageable() {
        // Given
        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                null, null, null, null, null, false, false, null, null, null, null, null, null, null, null
        );
        Pageable sortedByYear = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "productionYear"));

        Page<MediaItem> page = new PageImpl<>(List.of(), sortedByYear, 0);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), any())).thenReturn(new java.util.HashMap<>());

        // When
        service.filterSeries(filters, sortedByYear);

        // Then
        verify(repository).findAll(ArgumentMatchers.<Specification<MediaItem>>any(), eq(sortedByYear));
    }

    @Test
    void testAutoRefresh_Disabled() {
        // Given
        UUID seriesId = UUID.randomUUID();
        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                seriesId, null, null, null, null, false, false, null, null, null, null, null, null, null, null
        );

        Page<MediaItem> page = new PageImpl<>(List.of(testSeries), pageable, 1);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_AUTO_REFRESH_ON_READ), eq(false))).thenReturn(false);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new java.util.HashMap<>());

        // When
        Page<ShowResponseDTO> result = service.filterSeries(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(mediaItemRefreshService, never()).refreshIfStale(any());
    }

    @Test
    void testAutoRefresh_EnabledWithSingleSeries() {
        // Given
        UUID seriesId = UUID.randomUUID();
        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                seriesId, null, null, null, null, false, false, null, null, null, null, null, null, null, null
        );

        Page<MediaItem> page = new PageImpl<>(List.of(testSeries), pageable, 1);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_AUTO_REFRESH_ON_READ), eq(false))).thenReturn(true);
        when(mediaItemRefreshService.refreshIfStale(testSeries)).thenReturn(testSeries);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new java.util.HashMap<>());

        // When
        Page<ShowResponseDTO> result = service.filterSeries(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(mediaItemRefreshService, times(1)).refreshIfStale(testSeries);
    }

    @Test
    void testAutoRefresh_EnabledButNoIdFilter() {
        // Given
        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                null, "Test", null, null, null, false, false, null, null, null, null, null, null, null, null
        );

        Page<MediaItem> page = new PageImpl<>(List.of(testSeries), pageable, 1);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_AUTO_REFRESH_ON_READ), eq(false))).thenReturn(true);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new java.util.HashMap<>());

        // When
        Page<ShowResponseDTO> result = service.filterSeries(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(mediaItemRefreshService, never()).refreshIfStale(any());
    }

    @Test
    void testAutoRefresh_SeriesRefreshedSuccessfully() {
        // Given
        UUID seriesId = UUID.randomUUID();
        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                seriesId, null, null, null, null, false, false, null, null, null, null, null, null, null, null
        );

        MediaItem refreshedSeries = new MediaItem(
                MediaType.SERIES,
                "Test Series (Updated)",
                "jf-789",
                "tmdb-789",
                "imdb-789",
                2024,
                45,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );

        Page<MediaItem> page = new PageImpl<>(List.of(testSeries), pageable, 1);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_AUTO_REFRESH_ON_READ), eq(false))).thenReturn(true);
        when(mediaItemRefreshService.refreshIfStale(testSeries)).thenReturn(refreshedSeries);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new java.util.HashMap<>());

        // When
        Page<ShowResponseDTO> result = service.filterSeries(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Series (Updated)", result.getContent().get(0).title());
        verify(mediaItemRefreshService, times(1)).refreshIfStale(testSeries);
    }

    @Test
    void testAutoRefresh_EmptyPage() {
        // Given
        UUID seriesId = UUID.randomUUID();
        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                seriesId, null, null, null, null, false, false, null, null, null, null, null, null, null, null
        );

        Page<MediaItem> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(emptyPage);
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_AUTO_REFRESH_ON_READ), eq(false))).thenReturn(true);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new java.util.HashMap<>());

        // When
        Page<ShowResponseDTO> result = service.filterSeries(filters, pageable);

        // Then
        assertTrue(result.isEmpty());
        verify(mediaItemRefreshService, never()).refreshIfStale(any());
    }

    @Test
    void testAutoRefresh_JellyfinSeries() {
        // Given
        UUID seriesId = UUID.randomUUID();
        MediaItem jellyfinSeries = new MediaItem(
                MediaType.SERIES,
                "Jellyfin Series",
                "jf-123",
                "tmdb-123",
                "imdb-123",
                2024,
                45,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );

        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                seriesId, null, null, null, null, false, false, null, null, null, null, null, null, null, null
        );

        Page<MediaItem> page = new PageImpl<>(List.of(jellyfinSeries), pageable, 1);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_AUTO_REFRESH_ON_READ), eq(false))).thenReturn(true);
        when(mediaItemRefreshService.refreshIfStale(jellyfinSeries)).thenReturn(jellyfinSeries);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new java.util.HashMap<>());

        // When
        Page<ShowResponseDTO> result = service.filterSeries(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(mediaItemRefreshService, times(1)).refreshIfStale(jellyfinSeries);
    }
}
