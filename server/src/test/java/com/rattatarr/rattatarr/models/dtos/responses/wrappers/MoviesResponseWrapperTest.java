package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.rattatarr.rattatarr.models.dtos.responses.MovieResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MoviesResponseWrapperTest {

    @Test
    void fromPage_shouldCreateWrapperFromPage() {
        // Given
        MovieResponseDTO movie = new MovieResponseDTO(
                UUID.randomUUID(), "jf-123", "Test Movie", "123", "tt123",
                2023, 120, null, Set.of(), null, null, null, null
        );
        Page<MovieResponseDTO> page = new PageImpl<>(List.of(movie), PageRequest.of(0, 20), 1);

        // When
        MoviesResponseWrapper wrapper = MoviesResponseWrapper.fromPage(page);

        // Then
        assertNotNull(wrapper);
        assertEquals(1, wrapper.movies().size());
        assertEquals("Test Movie", wrapper.movies().getFirst().title());
        assertNotNull(wrapper.pagination());
        assertEquals(0, wrapper.pagination().currentPage());
        assertEquals(20, wrapper.pagination().pageSize());
        assertEquals(1, wrapper.pagination().totalElements());
    }

    @Test
    void fromList_shouldCreateWrapperFromList() {
        // Given
        MovieResponseDTO movie = new MovieResponseDTO(
                UUID.randomUUID(), "jf-123", "Test Movie", "123", "tt123",
                2023, 120, null, Set.of(), null, null, null, null
        );
        List<MovieResponseDTO> movies = List.of(movie);

        // When
        MoviesResponseWrapper wrapper = MoviesResponseWrapper.fromList(movies);

        // Then
        assertNotNull(wrapper);
        assertEquals(1, wrapper.movies().size());
        assertNull(wrapper.pagination());
    }
}
