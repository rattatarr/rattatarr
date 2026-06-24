package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.dtos.requests.SeriesFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.GenericResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.ShowResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.BrokenMediaItemResponseWrapper;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.SeriesResponseWrapper;
import com.rattatarr.rattatarr.models.entities.BrokenMediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LibrarySeriesControllerTest {
    @Mock
    private SeriesService seriesService;
    @Mock
    private BrokenMediaItemsService brokenMediaItemsService;
    @Mock
    private MediaItemsService mediaItemsService;
    @Mock
    private MediaItemRefreshService mediaItemRefreshService;
    @Mock
    private ProfilesService profilesService;

    @InjectMocks
    private LibrarySeriesController controller;

    private UUID seriesId;
    private MediaItem tmdbSeries;

    @BeforeEach
    void setUp() {
        seriesId = UUID.randomUUID();
        tmdbSeries = new MediaItem(
                MediaType.SERIES, "TMDb Series", null, "tmdb123", "imdb123",
                2022, 45, new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>()
        );
    }

    @Test
    void getSeries_shouldReturnSeries() {
        Pageable pageable = PageRequest.of(0, 20);
        SeriesFiltersDTO filters = new SeriesFiltersDTO(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        ShowResponseDTO series = new ShowResponseDTO(
                UUID.randomUUID(), "jf-123", "Test Series", "123", "tt123",
                2023, 60, null, Set.of(), null, null, null, null
        );
        Page<ShowResponseDTO> seriesPage = new PageImpl<>(List.of(series), pageable, 1);
        when(seriesService.filterSeries(any(SeriesFiltersDTO.class), any(Pageable.class))).thenReturn(seriesPage);

        ResponseEntity<SeriesResponseWrapper> response = controller.getSeries(pageable, filters);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().series().size());
        verify(seriesService).filterSeries(eq(filters), eq(pageable));
    }

    @Test
    void getSeries_shouldIncludeCreditsWhenIdProvided() {
        UUID id = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        SeriesFiltersDTO filters = new SeriesFiltersDTO(id, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        ShowResponseDTO series = new ShowResponseDTO(
                UUID.randomUUID(), "jf-123", "Test Series", "123", "tt123",
                2023, 60, null, Set.of(), null, null, null, null
        );
        Page<ShowResponseDTO> seriesPage = new PageImpl<>(List.of(series), pageable, 1);
        when(seriesService.filterSeries(any(SeriesFiltersDTO.class), any(Pageable.class))).thenReturn(seriesPage);

        ResponseEntity<SeriesResponseWrapper> response = controller.getSeries(pageable, filters);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getSeries_shouldHandleEmptyResults() {
        Pageable pageable = PageRequest.of(0, 20);
        SeriesFiltersDTO filters = new SeriesFiltersDTO(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        Page<ShowResponseDTO> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(seriesService.filterSeries(any(SeriesFiltersDTO.class), any(Pageable.class))).thenReturn(emptyPage);

        ResponseEntity<SeriesResponseWrapper> response = controller.getSeries(pageable, filters);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().series().size());
    }

    @Test
    void getBrokenSeries_shouldReturnBrokenSeries() {
        Pageable pageable = PageRequest.of(0, 20);
        BrokenMediaItem brokenSeries = new BrokenMediaItem(
                MediaType.SERIES, "Broken Series", "jf-123", null, null, 2023, "Missing TMDb ID"
        );
        Page<BrokenMediaItem> brokenPage = new PageImpl<>(List.of(brokenSeries), pageable, 1);
        when(brokenMediaItemsService.filterBrokenMediaItems(any(), any(Pageable.class))).thenReturn(brokenPage);

        ResponseEntity<BrokenMediaItemResponseWrapper> response = controller.getBrokenSeries(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().movies().size());
    }

    @Test
    void getBrokenSeries_shouldHandleEmptyResults() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<BrokenMediaItem> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(brokenMediaItemsService.filterBrokenMediaItems(any(), any(Pageable.class))).thenReturn(emptyPage);

        ResponseEntity<BrokenMediaItemResponseWrapper> response = controller.getBrokenSeries(pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().movies().size());
    }

    @Test
    void refreshMediaItem_shouldRefreshTMDbSeries() {
        when(mediaItemsService.findById(seriesId)).thenReturn(Optional.of(tmdbSeries));
        when(mediaItemRefreshService.refresh(tmdbSeries)).thenReturn(tmdbSeries);

        GenericResponseDTO result = controller.refreshMediaItem(seriesId);

        assertEquals(HttpStatus.OK, result.status());
        assertTrue(result.message().contains("refreshed successfully"));
        assertTrue(result.message().contains("TMDb Series"));
        verify(mediaItemsService).findById(seriesId);
        verify(mediaItemRefreshService).refresh(tmdbSeries);
    }

    @Test
    void refreshMediaItem_shouldHandleSeriesNotFound() {
        when(mediaItemsService.findById(seriesId)).thenReturn(Optional.empty());

        GenericResponseDTO result = controller.refreshMediaItem(seriesId);

        assertEquals(HttpStatus.BAD_REQUEST, result.status());
        assertTrue(result.message().contains("Refresh failed"));
        verify(mediaItemRefreshService, never()).refresh(any());
    }

    @Test
    void refreshMediaItem_shouldHandleRefreshError() {
        when(mediaItemsService.findById(seriesId)).thenReturn(Optional.of(tmdbSeries));
        when(mediaItemRefreshService.refresh(tmdbSeries)).thenThrow(new RuntimeException("TMDb API error"));

        GenericResponseDTO result = controller.refreshMediaItem(seriesId);

        assertEquals(HttpStatus.BAD_REQUEST, result.status());
        assertTrue(result.message().contains("Refresh failed"));
        assertTrue(result.message().contains("TMDb API error"));
    }

    @Test
    void refreshAllStaleItems_shouldTriggerAsyncRefreshAndReturnImmediately() {
        GenericResponseDTO result = controller.refreshAllStaleItems();

        assertEquals(HttpStatus.ACCEPTED, result.status());
        assertTrue(result.message().contains("started in background"));
        verify(mediaItemRefreshService).refreshAllStaleSeriesAsync();
    }

    @Test
    void getRecentlyWatchedUnratedSeries_shouldReturnSeries() {
        UUID profileId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        SeriesFiltersDTO filters = new SeriesFiltersDTO(
                null, null, null, null, null,
                false, false,
                "w500", "w1280", "w185",
                profileId, null, null, null, null
        );
        ShowResponseDTO series = new ShowResponseDTO(
                UUID.randomUUID(), "jf-123", "Test Series", "123", "tt123",
                2023, 60, null, Set.of(), null, null, null, null
        );
        Page<ShowResponseDTO> seriesPage = new PageImpl<>(List.of(series), pageable, 1);

        when(seriesService.findRecentlyWatchedUnratedSeries(filters, pageable)).thenReturn(seriesPage);

        ResponseEntity<SeriesResponseWrapper> response = controller.getRecentlyWatchedUnratedSeries(pageable, filters);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().series().size());
        verify(seriesService).findRecentlyWatchedUnratedSeries(filters, pageable);
    }
}
