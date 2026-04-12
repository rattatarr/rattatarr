package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbMovieFullDetailsResponseDTO;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbShowFullDetailsResponseDTO;
import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.dtos.requests.ImportMediaItemRequestDTO;
import com.rattatarr.rattatarr.models.dtos.requests.SearchFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.GenericResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.ImportMediaItemResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.SearchTMDbResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.SearchGroupTMDbWrapper;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.SearchTMDbWrapper;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.services.MediaItemCreditsService;
import com.rattatarr.rattatarr.services.TMDbService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TMDbControllerTest {

    @Mock
    private TMDbService tmDbService;
    @Mock
    private MediaItemCreditsService mediaItemCreditsService;

    @InjectMocks
    private TMDbController tmDbController;

    @Test
    void testConnection_shouldReturnSuccessWhenHealthy() {
        // Given
        when(tmDbService.testConnection()).thenReturn(true);

        // When
        GenericResponseDTO result = tmDbController.testConnection();

        // Then
        assertNotNull(result);
        assertTrue(result.message().contains("successful"));
        verify(tmDbService).testConnection();
    }

    @Test
    void testConnection_shouldReturnFailureWhenUnhealthy() {
        // Given
        when(tmDbService.testConnection()).thenReturn(false);

        // When
        GenericResponseDTO result = tmDbController.testConnection();

        // Then
        assertNotNull(result);
        assertTrue(result.message().contains("failed"));
        verify(tmDbService).testConnection();
    }

    @Test
    void testConnection_shouldHandleError() {
        // Given
        when(tmDbService.testConnection())
                .thenThrow(new CommonExceptions.InvalidRequestExceptions("Network error"));

        // When & Then
        assertThrows(CommonExceptions.InvalidRequestExceptions.class, () ->
                tmDbController.testConnection()
        );
        verify(tmDbService).testConnection();
    }

    @Test
    void buildCredits_shouldInitiateCreditsBuilding() {
        GenericResponseDTO result = tmDbController.buildCredits(true);

        assertNotNull(result);
        assertEquals("TMDb credits build initiated", result.message());
        assertEquals(HttpStatus.ACCEPTED, result.status());
        verify(mediaItemCreditsService).triggerBackgroundCreditsUpdate(true);
    }

    @Test
    void buildCredits_shouldUseDefaultForceRefreshFalse() {
        GenericResponseDTO result = tmDbController.buildCredits(false);

        assertNotNull(result);
        verify(mediaItemCreditsService).triggerBackgroundCreditsUpdate(false);
    }

    @Test
    void getMovieDetails_shouldReturnMovieDetails() {
        String tmdbId = "123";
        TMDbMovieFullDetailsResponseDTO movieDetails = mock(TMDbMovieFullDetailsResponseDTO.class);
        when(tmDbService.findMovieById(tmdbId)).thenReturn(movieDetails);

        TMDbMovieFullDetailsResponseDTO result = tmDbController.getMovieDetails(tmdbId);

        assertNotNull(result);
        assertEquals(movieDetails, result);
        verify(tmDbService).findMovieById(tmdbId);
    }

    @Test
    void getMovieDetails_shouldHandleError() {
        String tmdbId = "invalid";
        when(tmDbService.findMovieById(tmdbId))
                .thenThrow(new CommonExceptions.ResourceNotFoundExceptions("Movie not found"));

        assertThrows(CommonExceptions.ResourceNotFoundExceptions.class, () ->
                tmDbController.getMovieDetails(tmdbId)
        );
        verify(tmDbService).findMovieById(tmdbId);
    }

    @Test
    void getShowDetails_shouldReturnShowDetails() {
        String tmdbId = "456";
        TMDbShowFullDetailsResponseDTO showDetails = mock(TMDbShowFullDetailsResponseDTO.class);
        when(tmDbService.findShowById(tmdbId)).thenReturn(showDetails);

        TMDbShowFullDetailsResponseDTO result = tmDbController.getShowDetails(tmdbId);

        assertNotNull(result);
        assertEquals(showDetails, result);
        verify(tmDbService).findShowById(tmdbId);
    }

    @Test
    void getShowDetails_shouldHandleError() {
        String tmdbId = "invalid";
        when(tmDbService.findShowById(tmdbId))
                .thenThrow(new CommonExceptions.ResourceNotFoundExceptions("Show not found"));

        assertThrows(CommonExceptions.ResourceNotFoundExceptions.class, () ->
                tmDbController.getShowDetails(tmdbId)
        );
        verify(tmDbService).findShowById(tmdbId);
    }

    @Test
    void searchTMDbByName_shouldReturnGroupedResults() {
        SearchFiltersDTO filters = new SearchFiltersDTO("Inception", null, 1);
        SearchTMDbResponseDTO movieResult = new SearchTMDbResponseDTO(123, "Inception", "2010", "poster.jpg", null, null);
        SearchTMDbResponseDTO seriesResult = new SearchTMDbResponseDTO(456, "Inception Series", "2020", "poster2.jpg", null, null);
        SearchGroupTMDbWrapper expectedWrapper = new SearchGroupTMDbWrapper(List.of(movieResult), List.of(seriesResult));

        when(tmDbService.searchByName(filters)).thenReturn(expectedWrapper);

        SearchGroupTMDbWrapper result = tmDbController.searchTMDbByName(filters);

        assertNotNull(result);
        assertEquals(1, result.movies().size());
        assertEquals(1, result.series().size());
        verify(tmDbService).searchByName(filters);
    }

    @Test
    void searchTMDbByName_shouldHandleEmptyResults() {
        SearchFiltersDTO filters = new SearchFiltersDTO("NonExistentMovie", null, 1);
        SearchGroupTMDbWrapper emptyWrapper = new SearchGroupTMDbWrapper(List.of(), List.of());
        when(tmDbService.searchByName(filters)).thenReturn(emptyWrapper);

        SearchGroupTMDbWrapper result = tmDbController.searchTMDbByName(filters);

        assertNotNull(result);
        assertTrue(result.movies().isEmpty());
        assertTrue(result.series().isEmpty());
    }

    @Test
    void searchMoviesByName_shouldReturnMovieResults() {
        SearchFiltersDTO filters = new SearchFiltersDTO("Matrix", null, 1);
        SearchTMDbResponseDTO movie1 = new SearchTMDbResponseDTO(123, "The Matrix", "1999", "poster1.jpg", null, null);
        SearchTMDbResponseDTO movie2 = new SearchTMDbResponseDTO(124, "The Matrix Reloaded", "2003", "poster2.jpg", null, null);
        SearchTMDbWrapper expectedWrapper = new SearchTMDbWrapper(List.of(movie1, movie2), 2, 1, 1);
        when(tmDbService.searchMoviesByName(filters)).thenReturn(expectedWrapper);

        SearchTMDbWrapper result = tmDbController.searchMoviesByName(filters);

        assertNotNull(result);
        assertEquals(2, result.results().size());
        verify(tmDbService).searchMoviesByName(filters);
    }

    @Test
    void searchMoviesByName_shouldHandleEmptyResults() {
        SearchFiltersDTO filters = new SearchFiltersDTO("NonExistentMovie", null, 1);
        SearchTMDbWrapper emptyWrapper = new SearchTMDbWrapper(List.of(), 0, 0, 1);
        when(tmDbService.searchMoviesByName(filters)).thenReturn(emptyWrapper);

        SearchTMDbWrapper result = tmDbController.searchMoviesByName(filters);

        assertNotNull(result);
        assertTrue(result.results().isEmpty());
    }

    @Test
    void searchTVSeriesByName_shouldReturnSeriesResults() {
        SearchFiltersDTO filters = new SearchFiltersDTO("Breaking Bad", null, 1);
        SearchTMDbResponseDTO series1 = new SearchTMDbResponseDTO(456, "Breaking Bad", "2008", "poster.jpg", null, null);
        SearchTMDbWrapper expectedWrapper = new SearchTMDbWrapper(List.of(series1), 1, 1, 1);
        when(tmDbService.searchSeriesByName(filters)).thenReturn(expectedWrapper);

        SearchTMDbWrapper result = tmDbController.searchTVSeriesByName(filters);

        assertNotNull(result);
        assertEquals(1, result.results().size());
        assertEquals("Breaking Bad", result.results().get(0).title());
    }

    @Test
    void searchTVSeriesByName_shouldHandleEmptyResults() {
        SearchFiltersDTO filters = new SearchFiltersDTO("NonExistentSeries", null, 1);
        SearchTMDbWrapper emptyWrapper = new SearchTMDbWrapper(List.of(), 0, 0, 1);
        when(tmDbService.searchSeriesByName(filters)).thenReturn(emptyWrapper);

        SearchTMDbWrapper result = tmDbController.searchTVSeriesByName(filters);

        assertNotNull(result);
        assertTrue(result.results().isEmpty());
    }

    @Test
    void importTMDbData_shouldImportMovieSuccessfully() {
        ImportMediaItemRequestDTO requestDTO = new ImportMediaItemRequestDTO("123", MediaType.MOVIE);
        MediaItem mediaItem = mock(MediaItem.class);
        UUID mediaItemId = java.util.UUID.randomUUID();
        when(mediaItem.id()).thenReturn(mediaItemId);
        when(mediaItem.title()).thenReturn("Inception");
        when(mediaItem.TMDbId()).thenReturn("123");
        when(mediaItem.mediaType()).thenReturn(MediaType.MOVIE);
        when(tmDbService.importMediaItem("123", MediaType.MOVIE)).thenReturn(mediaItem);

        ImportMediaItemResponseDTO result = tmDbController.importTMDbData(requestDTO);

        assertNotNull(result);
        assertEquals(mediaItemId, result.id());
        assertEquals("Inception", result.title());
        assertEquals("123", result.TMDbId());
        assertEquals(MediaType.MOVIE, result.mediaType());
        verify(tmDbService).importMediaItem("123", MediaType.MOVIE);
    }

    @Test
    void importTMDbData_shouldImportSeriesSuccessfully() {
        ImportMediaItemRequestDTO requestDTO = new ImportMediaItemRequestDTO("456", MediaType.SERIES);
        MediaItem mediaItem = mock(MediaItem.class);
        UUID mediaItemId = java.util.UUID.randomUUID();
        when(mediaItem.id()).thenReturn(mediaItemId);
        when(mediaItem.title()).thenReturn("Breaking Bad");
        when(mediaItem.TMDbId()).thenReturn("456");
        when(mediaItem.mediaType()).thenReturn(MediaType.SERIES);
        when(tmDbService.importMediaItem("456", MediaType.SERIES)).thenReturn(mediaItem);

        ImportMediaItemResponseDTO result = tmDbController.importTMDbData(requestDTO);

        assertNotNull(result);
        assertEquals(mediaItemId, result.id());
        assertEquals("Breaking Bad", result.title());
        assertEquals("456", result.TMDbId());
        assertEquals(MediaType.SERIES, result.mediaType());
        verify(tmDbService).importMediaItem("456", MediaType.SERIES);
    }

    @Test
    void importTMDbData_shouldHandleImportError() {
        ImportMediaItemRequestDTO requestDTO = new ImportMediaItemRequestDTO("invalid", MediaType.MOVIE);
        when(tmDbService.importMediaItem("invalid", MediaType.MOVIE))
                .thenThrow(new CommonExceptions.InvalidRequestExceptions("Import failed: Invalid ID"));

        assertThrows(CommonExceptions.InvalidRequestExceptions.class, () ->
                tmDbController.importTMDbData(requestDTO)
        );
        verify(tmDbService).importMediaItem("invalid", MediaType.MOVIE);
    }

    @Test
    void importTMDbData_shouldHandleNetworkError() {
        ImportMediaItemRequestDTO requestDTO = new ImportMediaItemRequestDTO("789", MediaType.SERIES);
        when(tmDbService.importMediaItem("789", MediaType.SERIES))
                .thenThrow(new RuntimeException("Network timeout"));

        assertThrows(RuntimeException.class, () ->
                tmDbController.importTMDbData(requestDTO)
        );
        verify(tmDbService).importMediaItem("789", MediaType.SERIES);
    }
}
