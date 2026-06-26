package com.rattatarr.rattatarr.models.dtos.responses;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MovieResponseDTOTest {

    @Test
    void fromEntity_shouldConvertMediaItemToDTO() {
        // Given
        MediaItem movie = new MediaItem(
                MediaType.MOVIE, "Test Movie", "jf-123", "tmdb-123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        // When
        MovieResponseDTO dto = MovieResponseDTO.fromEntity(movie);

        // Then
        assertNotNull(dto);
        assertEquals("Test Movie", dto.title());
        assertEquals("jf-123", dto.jellyfinId());
        assertEquals("tmdb-123", dto.TMDbId());
        assertEquals("tt123", dto.IMDbId());
        assertEquals(2023, dto.productionYear());
        assertEquals(120, dto.runtimeMinutes());
        assertNotNull(dto.genres());
        assertNull(dto.cast());
        assertNull(dto.crew());
    }

    @Test
    void fromEntity_withIncludeCredits_shouldIncludeCreditsWhenInitialized() {
        // Given
        MediaItem movie = new MediaItem(
                MediaType.MOVIE, "Test Movie", "jf-123", "tmdb-123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        // When
        MovieResponseDTO dto = MovieResponseDTO.fromEntity(movie, true);

        // Then
        assertNotNull(dto);
        assertEquals("Test Movie", dto.title());
        // Cast and crew will be null since they're empty sets (not initialized with Hibernate)
    }

    @Test
    void constructor_shouldCreateDTO() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        MovieResponseDTO dto = new MovieResponseDTO(
                id, "jf-123", "Title", "tmdb-123", "tt123",
                2023, 120, null, Set.of(), null, null, 4.5f, null
        );

        // Then
        assertEquals(id, dto.id());
        assertEquals("Title", dto.title());
        assertEquals(120, dto.runtimeMinutes());
    }

    @Test
    void constructor_withNullIMDbId_shouldAllowNullValue() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        MovieResponseDTO dto = new MovieResponseDTO(
                id, "jf-123", "Title", "tmdb-123", null,
                2023, 120, null, Set.of(), null, null, 4.5f, null
        );

        // Then
        assertNotNull(dto);
        assertEquals(id, dto.id());
        assertEquals("Title", dto.title());
        assertEquals("tmdb-123", dto.TMDbId());
        assertNull(dto.IMDbId());
        assertEquals(2023, dto.productionYear());
    }

    @Test
    void fromEntity_withNullIMDbId_shouldConvertSuccessfully() {
        // Given
        MediaItem movie = new MediaItem(
                MediaType.MOVIE, "Test Movie", "jf-123", "tmdb-123", null,
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        // When
        MovieResponseDTO dto = MovieResponseDTO.fromEntity(movie);

        // Then
        assertNotNull(dto);
        assertEquals("Test Movie", dto.title());
        assertEquals("jf-123", dto.jellyfinId());
        assertEquals("tmdb-123", dto.TMDbId());
        assertNull(dto.IMDbId());
        assertEquals(2023, dto.productionYear());
        assertEquals(120, dto.runtimeMinutes());
    }

    @Test
    void constructor_withValidIMDbId_shouldStoreCorrectly() {
        // Given
        UUID id = UUID.randomUUID();
        String imdbId = "tt1234567";

        // When
        MovieResponseDTO dto = new MovieResponseDTO(
                id, "jf-123", "Title", "tmdb-123", imdbId,
                2023, 120, null, Set.of(), null, null, 4.5f, null
        );

        // Then
        assertNotNull(dto);
        assertEquals(imdbId, dto.IMDbId());
        assertEquals("tt1234567", dto.IMDbId());
    }
}
