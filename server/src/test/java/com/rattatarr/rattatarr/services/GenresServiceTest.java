package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbGenreResponseDTO;
import com.rattatarr.rattatarr.models.dtos.requests.GenresFiltersDTO;
import com.rattatarr.rattatarr.models.entities.Genre;
import com.rattatarr.rattatarr.repositories.GenresRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenresServiceTest {

    @Mock
    private GenresRepository repository;

    @InjectMocks
    private GenresService service;

    private Genre testGenre;

    @BeforeEach
    void setUp() {
        testGenre = new Genre("Action");
    }

    @Test
    void testFindOrCreateByName_ExistingGenre() {
        // Given
        when(repository.findByName("Action")).thenReturn(Optional.of(testGenre));

        // When
        Genre result = service.findOrCreateByName("Action");

        // Then
        assertNotNull(result);
        assertEquals("Action", result.name());
        verify(repository).findByName("Action");
        verify(repository, never()).save(any(Genre.class));
    }

    @Test
    void testFindOrCreateByName_NewGenre() {
        // Given
        when(repository.findByName("Comedy")).thenReturn(Optional.empty());
        when(repository.save(any(Genre.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Genre result = service.findOrCreateByName("Comedy");

        // Then
        assertNotNull(result);
        assertEquals("Comedy", result.name());
        verify(repository).findByName("Comedy");
        verify(repository).save(any(Genre.class));
    }

    @Test
    void testFindOrCreateByNameCached_Cached() {
        // Given
        when(repository.findByName("Action")).thenReturn(Optional.of(testGenre));

        // When - Call twice to test caching
        Genre firstResult = service.findOrCreateByNameCached("Action");
        Genre secondResult = service.findOrCreateByNameCached("Action");

        // Then
        assertNotNull(firstResult);
        assertNotNull(secondResult);
        assertEquals("Action", firstResult.name());
        assertEquals("Action", secondResult.name());

        // Should only call repository once due to caching
        verify(repository, times(1)).findByName("Action");
    }

    @Test
    void testFilterGenres() {
        // Given
        GenresFiltersDTO filters = new GenresFiltersDTO("Act");
        Pageable pageable = PageRequest.of(0, 20);
        List<Genre> genres = List.of(testGenre, new Genre("Action-Adventure"));
        Page<Genre> page = new PageImpl<>(genres, pageable, 2);

        when(repository.findAll(ArgumentMatchers.<Specification<Genre>>any(), any(Pageable.class)))
                .thenReturn(page);

        // When
        Page<Genre> result = service.filterGenres(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(repository).findAll(ArgumentMatchers.<Specification<Genre>>any(), eq(pageable));
    }

    @Test
    void testFilterGenres_EmptyResult() {
        // Given
        GenresFiltersDTO filters = new GenresFiltersDTO("NonExistent");
        Pageable pageable = PageRequest.of(0, 20);
        Page<Genre> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(repository.findAll(ArgumentMatchers.<Specification<Genre>>any(), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        Page<Genre> result = service.filterGenres(filters, pageable);

        // Then
        assertTrue(result.isEmpty());
        verify(repository).findAll(ArgumentMatchers.<Specification<Genre>>any(), any(Pageable.class));
    }

    @Test
    void testUpsertGenresFromTMDbDTOs_AllNew() {
        // Given
        List<TMDbGenreResponseDTO> dtos = List.of(
                new TMDbGenreResponseDTO(28, "Action"),
                new TMDbGenreResponseDTO(35, "Comedy")
        );
        Set<String> genreNames = Set.of("Action", "Comedy");

        when(repository.findAllByNameIn(genreNames)).thenReturn(Set.of());
        when(repository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<?> argument = invocation.getArgument(0);
            return List.copyOf((Set<?>) argument);
        });

        // When
        Set<Genre> result = service.upsertGenresFromTMDbDTOs(dtos);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository).findAllByNameIn(genreNames);
        verify(repository).saveAll(any());
    }

    @Test
    void testUpsertGenresFromTMDbDTOs_AllExisting() {
        // Given
        Genre action = new Genre("Action");
        Genre comedy = new Genre("Comedy");
        List<TMDbGenreResponseDTO> dtos = List.of(
                new TMDbGenreResponseDTO(28, "Action"),
                new TMDbGenreResponseDTO(35, "Comedy")
        );
        Set<String> genreNames = Set.of("Action", "Comedy");

        when(repository.findAllByNameIn(genreNames)).thenReturn(Set.of(action, comedy));

        // When
        Set<Genre> result = service.upsertGenresFromTMDbDTOs(dtos);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository).findAllByNameIn(genreNames);
        verify(repository, never()).saveAll(any());
    }

    @Test
    void testUpsertGenresFromTMDbDTOs_Mixed() {
        // Given
        Genre action = new Genre("Action");
        List<TMDbGenreResponseDTO> dtos = List.of(
                new TMDbGenreResponseDTO(28, "Action"),
                new TMDbGenreResponseDTO(35, "Comedy")
        );
        Set<String> genreNames = Set.of("Action", "Comedy");

        when(repository.findAllByNameIn(genreNames)).thenReturn(Set.of(action));
        when(repository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<?> argument = invocation.getArgument(0);
            return List.copyOf((Set<?>) argument);
        });

        // When
        Set<Genre> result = service.upsertGenresFromTMDbDTOs(dtos);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository).findAllByNameIn(genreNames);
        verify(repository).saveAll(any());
    }

    @Test
    void testUpsertGenresFromTMDbDTOs_EmptyInput() {
        // Given
        List<TMDbGenreResponseDTO> dtos = List.of();

        // When
        Set<Genre> result = service.upsertGenresFromTMDbDTOs(dtos);

        // Then
        assertTrue(result.isEmpty());
        verify(repository, never()).findAllByNameIn(any());
        verify(repository, never()).saveAll(any());
    }
}
