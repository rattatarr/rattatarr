package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.dtos.requests.MoviesFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.MovieResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.BrokenMediaItemResponseWrapper;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.MoviesResponseWrapper;
import com.rattatarr.rattatarr.models.entities.BrokenMediaItem;
import com.rattatarr.rattatarr.services.BrokenMediaItemsService;
import com.rattatarr.rattatarr.services.MoviesService;
import com.rattatarr.rattatarr.services.ProfilesService;
import com.rattatarr.rattatarr.services.RadarrService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryMoviesControllerTest {

    @Mock
    private MoviesService moviesService;

    @Mock
    private BrokenMediaItemsService brokenMediaItemsService;

    @Mock
    private RadarrService radarrService;

    @Mock
    private ProfilesService profilesService;

    @InjectMocks
    private LibraryMoviesController controller;

    @Test
    void getMovies_shouldReturnMovies() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        MoviesFiltersDTO filters = new MoviesFiltersDTO(null, null, null, null, null, null, null, null, null, null, null, null, null);

        MovieResponseDTO movie = new MovieResponseDTO(
                UUID.randomUUID(), "jf-123", "Test Movie", "123", "tt123",
                2023, 120, null, Set.of(), null, null, null
        );
        Page<MovieResponseDTO> moviesPage = new PageImpl<>(List.of(movie), pageable, 1);

        when(moviesService.filterMovies(any(MoviesFiltersDTO.class), any(Pageable.class)))
                .thenReturn(moviesPage);

        // When
        ResponseEntity<MoviesResponseWrapper> response = controller.getMovies(pageable, filters);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().movies().size());
        verify(moviesService).filterMovies(eq(filters), eq(pageable));
    }

    @Test
    void getMovies_shouldIncludeCreditsWhenIdProvided() {
        // Given
        UUID movieId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        MoviesFiltersDTO filters = new MoviesFiltersDTO(movieId, null, null, null, null, null, null, null, null, null, null, null, null);

        MovieResponseDTO movie = new MovieResponseDTO(
                movieId, "jf-123", "Test Movie", "123", "tt123",
                2023, 120, null, Set.of(), null, null, null
        );
        Page<MovieResponseDTO> moviesPage = new PageImpl<>(List.of(movie), pageable, 1);

        when(moviesService.filterMovies(any(MoviesFiltersDTO.class), any(Pageable.class)))
                .thenReturn(moviesPage);

        // When
        ResponseEntity<MoviesResponseWrapper> response = controller.getMovies(pageable, filters);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(moviesService).filterMovies(eq(filters), eq(pageable));
    }

    @Test
    void getMovies_shouldHandleEmptyResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        MoviesFiltersDTO filters = new MoviesFiltersDTO(null, null, null, null, null, null, null, null, null, null, null, null, null);
        Page<MovieResponseDTO> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(moviesService.filterMovies(any(MoviesFiltersDTO.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        ResponseEntity<MoviesResponseWrapper> response = controller.getMovies(pageable, filters);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().movies().size());
    }

    @Test
    void getBrokenMovies_shouldReturnBrokenMovies() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        BrokenMediaItem brokenMovie = new BrokenMediaItem(
                MediaType.MOVIE, "Broken Movie", "jf-123", null, null, 2023, "Missing TMDb ID"
        );
        Page<BrokenMediaItem> brokenPage = new PageImpl<>(List.of(brokenMovie), pageable, 1);

        when(brokenMediaItemsService.filterBrokenMediaItems(any(), any(Pageable.class)))
                .thenReturn(brokenPage);

        // When
        ResponseEntity<BrokenMediaItemResponseWrapper> response = controller.getBrokenMovies(pageable);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().movies().size());
        verify(brokenMediaItemsService).filterBrokenMediaItems(any(), eq(pageable));
    }

    @Test
    void getBrokenMovies_shouldHandleEmptyResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        Page<BrokenMediaItem> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(brokenMediaItemsService.filterBrokenMediaItems(any(), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        ResponseEntity<BrokenMediaItemResponseWrapper> response = controller.getBrokenMovies(pageable);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().movies().size());
    }

    @Test
    void getRecentlyWatchedUnratedMovies_shouldReturnMovies() {
        UUID profileId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 20);
        MoviesFiltersDTO filters = new MoviesFiltersDTO(
                null, null, null, null, null,
                "w500", "w1280", "w185",
                profileId, null, null, null, null
        );

        MovieResponseDTO movie = new MovieResponseDTO(
                UUID.randomUUID(), "jf-123", "Test Movie", "123", "tt123",
                2023, 120, null, Set.of(), null, null, null
        );
        Page<MovieResponseDTO> moviesPage = new PageImpl<>(List.of(movie), pageable, 1);

        when(moviesService.findRecentlyWatchedUnratedMovies(filters, pageable)).thenReturn(moviesPage);

        ResponseEntity<MoviesResponseWrapper> response = controller.getRecentlyWatchedUnratedMovies(pageable, filters);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().movies().size());
        verify(moviesService).findRecentlyWatchedUnratedMovies(filters, pageable);
    }
}
