package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.models.dtos.requests.GenresFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.GenresWrapper;
import com.rattatarr.rattatarr.models.entities.Genre;
import com.rattatarr.rattatarr.services.GenresService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibraryGenresControllerTest {

    @Mock
    private GenresService genresService;

    @InjectMocks
    private LibraryGenresController controller;

    @Test
    void getSeries_shouldReturnGenres() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        GenresFiltersDTO filters = new GenresFiltersDTO(null);

        Genre genre = new Genre("Action");
        Page<Genre> genresPage = new PageImpl<>(List.of(genre), pageable, 1);

        when(genresService.filterGenres(any(GenresFiltersDTO.class), any(Pageable.class)))
                .thenReturn(genresPage);

        // When
        ResponseEntity<GenresWrapper> response = controller.getGenres(pageable, filters);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().genres().size());
        verify(genresService).filterGenres(eq(filters), eq(pageable));
    }

    @Test
    void getSeries_shouldHandleEmptyResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);
        GenresFiltersDTO filters = new GenresFiltersDTO(null);
        Page<Genre> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(genresService.filterGenres(any(GenresFiltersDTO.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        ResponseEntity<GenresWrapper> response = controller.getGenres(pageable, filters);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().genres().size());
    }
}
