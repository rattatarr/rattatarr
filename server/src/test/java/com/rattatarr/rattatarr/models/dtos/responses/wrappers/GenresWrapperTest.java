package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.rattatarr.rattatarr.models.entities.Genre;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenresWrapperTest {

    @Test
    void fromPage_shouldCreateWrapperFromPage() {
        // Given
        Genre genre = new Genre("Action");
        Page<Genre> page = new PageImpl<>(List.of(genre), PageRequest.of(0, 20), 1);

        // When
        GenresWrapper wrapper = GenresWrapper.fromPage(page);

        // Then
        assertNotNull(wrapper);
        assertEquals(1, wrapper.genres().size());
        assertEquals("Action", wrapper.genres().getFirst().name());
        assertNotNull(wrapper.pagination());
        assertEquals(0, wrapper.pagination().currentPage());
        assertEquals(20, wrapper.pagination().pageSize());
        assertEquals(1, wrapper.pagination().totalElements());
    }

    @Test
    void fromList_shouldCreateWrapperFromList() {
        // Given
        Genre genre = new Genre("Drama");
        List<Genre> genres = List.of(genre);

        // When
        GenresWrapper wrapper = GenresWrapper.fromList(genres);

        // Then
        assertNotNull(wrapper);
        assertEquals(1, wrapper.genres().size());
        assertEquals("Drama", wrapper.genres().getFirst().name());
        assertNull(wrapper.pagination());
    }
}
