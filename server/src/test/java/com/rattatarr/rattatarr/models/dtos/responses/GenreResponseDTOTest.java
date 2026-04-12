package com.rattatarr.rattatarr.models.dtos.responses;

import com.rattatarr.rattatarr.models.entities.Genre;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GenreResponseDTOTest {

    @Test
    void fromEntity_shouldConvertGenreToDTO() {
        // Given
        Genre genre = new Genre("Action");

        // When
        GenreResponseDTO dto = GenreResponseDTO.fromEntity(genre);

        // Then
        assertNotNull(dto);
        assertEquals("Action", dto.name());
    }

    @Test
    void fromEntities_shouldConvertMultipleGenres() {
        // Given
        Genre genre1 = new Genre("Action");
        Genre genre2 = new Genre("Drama");
        Set<Genre> genres = Set.of(genre1, genre2);

        // When
        Set<GenreResponseDTO> dtos = GenreResponseDTO.fromEntities(genres);

        // Then
        assertEquals(2, dtos.size());
        assertTrue(dtos.stream().anyMatch(dto -> dto.name().equals("Action")));
        assertTrue(dtos.stream().anyMatch(dto -> dto.name().equals("Drama")));
    }

    @Test
    void constructor_shouldCreateDTO() {
        // Given
        java.util.UUID id = java.util.UUID.randomUUID();

        // When
        GenreResponseDTO dto = new GenreResponseDTO(id, "Comedy");

        // Then
        assertEquals(id, dto.id());
        assertEquals("Comedy", dto.name());
    }
}
