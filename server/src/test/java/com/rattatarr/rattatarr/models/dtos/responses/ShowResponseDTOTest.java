package com.rattatarr.rattatarr.models.dtos.responses;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ShowResponseDTOTest {

    @Test
    void fromEntity_shouldConvertMediaItemToDTO() {
        // Given
        MediaItem series = new MediaItem(
                MediaType.SERIES, "Test Series", "jf-456", "tmdb-456", "tt456",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );

        // When
        ShowResponseDTO dto = ShowResponseDTO.fromEntity(series);

        // Then
        assertNotNull(dto);
        assertEquals("Test Series", dto.title());
        assertEquals("jf-456", dto.jellyfinId());
        assertEquals("tmdb-456", dto.TMDbId());
        assertEquals("tt456", dto.IMDbId());
        assertEquals(2023, dto.productionYear());
        assertEquals(60, dto.runtimeMinutes());
        assertNotNull(dto.genres());
    }

    @Test
    void fromEntity_withIncludeCredits_shouldHandleCreditsParameter() {
        // Given
        MediaItem series = new MediaItem(
                MediaType.SERIES, "Test Series", "jf-456", "tmdb-456", "tt456",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );

        // When
        ShowResponseDTO dto = ShowResponseDTO.fromEntity(series, true);

        // Then
        assertNotNull(dto);
        assertEquals("Test Series", dto.title());
    }

    @Test
    void fromEntities_shouldConvertMultipleMediaItems() {
        // Given
        MediaItem series1 = new MediaItem(
                MediaType.SERIES, "Series 1", "jf-1", "tmdb-1", "tt1",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaItem series2 = new MediaItem(
                MediaType.SERIES, "Series 2", "jf-2", "tmdb-2", "tt2",
                2024, 45, Set.of(), Set.of(), Set.of(), Set.of()
        );
        Set<MediaItem> items = Set.of(series1, series2);

        // When
        var dtos = ShowResponseDTO.fromEntities(items);

        // Then
        assertEquals(2, dtos.size());
    }

    @Test
    void fromEntity_withIMDbId_shouldIncludeIMDbId() {
        // Given
        MediaItem series = new MediaItem(
                MediaType.SERIES, "Test Series with IMDb", "jf-789", "tmdb-789", "tt1234567",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );

        // When
        ShowResponseDTO dto = ShowResponseDTO.fromEntity(series);

        // Then
        assertNotNull(dto);
        assertEquals("tt1234567", dto.IMDbId());
        assertEquals("Test Series with IMDb", dto.title());
        assertEquals("jf-789", dto.jellyfinId());
        assertEquals("tmdb-789", dto.TMDbId());
    }

    @Test
    void fromEntity_withNullIMDbId_shouldAllowNullIMDbId() {
        // Given
        MediaItem series = new MediaItem(
                MediaType.SERIES, "Test Series without IMDb", "jf-999", "tmdb-999", null,
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );

        // When
        ShowResponseDTO dto = ShowResponseDTO.fromEntity(series);

        // Then
        assertNotNull(dto);
        assertNull(dto.IMDbId());
        assertEquals("Test Series without IMDb", dto.title());
        assertEquals("jf-999", dto.jellyfinId());
        assertEquals("tmdb-999", dto.TMDbId());
    }

    @Test
    void fromEntity_withVariousIMDbIdFormats_shouldHandleAllFormats() {
        // Given - IMDb IDs can have different formats
        MediaItem series1 = new MediaItem(
                MediaType.SERIES, "Series 1", "jf-1", "tmdb-1", "tt0123456",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaItem series2 = new MediaItem(
                MediaType.SERIES, "Series 2", "jf-2", "tmdb-2", "tt12345678",
                2024, 45, Set.of(), Set.of(), Set.of(), Set.of()
        );

        // When
        ShowResponseDTO dto1 = ShowResponseDTO.fromEntity(series1);
        ShowResponseDTO dto2 = ShowResponseDTO.fromEntity(series2);

        // Then
        assertEquals("tt0123456", dto1.IMDbId());
        assertEquals("tt12345678", dto2.IMDbId());
    }
}
