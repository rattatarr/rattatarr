package com.rattatarr.rattatarr.models.dtos.responses;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.BrokenMediaItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BrokenMediaItemResponseDTOTest {

    @Test
    void fromEntity_shouldConvertBrokenMediaItemToDTO() {
        // Given
        BrokenMediaItem brokenItem = new BrokenMediaItem(
                MediaType.MOVIE, "Broken Movie", "jf-123", null, null, 2023, "Missing TMDb ID"
        );

        // When
        BrokenMediaItemResponseDTO dto = BrokenMediaItemResponseDTO.fromEntity(brokenItem);

        // Then
        assertNotNull(dto);
        assertEquals("Broken Movie", dto.title());
        assertEquals("jf-123", dto.jellyfinId());
        assertNull(dto.TMDbId());
        assertEquals(2023, dto.productionYear());
        assertEquals("Missing TMDb ID", dto.missingFields());
        assertFalse(dto.resolved());
    }

    @Test
    void constructor_shouldCreateDTO() {
        // Given & When
        BrokenMediaItemResponseDTO dto = new BrokenMediaItemResponseDTO(
                java.util.UUID.randomUUID(), "Test", "jf-1", "tmdb-1", "tt1", 2023, "Missing fields", null, false
        );

        // Then
        assertEquals("Test", dto.title());
        assertFalse(dto.resolved());
    }
}
