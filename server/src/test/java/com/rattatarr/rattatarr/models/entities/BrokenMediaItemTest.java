package com.rattatarr.rattatarr.models.entities;

import com.rattatarr.rattatarr.models.MediaType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BrokenMediaItemTest {

    @Test
    void constructor_shouldCreateBrokenMediaItem() {
        // Given & When
        BrokenMediaItem item = new BrokenMediaItem(
                MediaType.MOVIE, "Broken Movie", "jf-123", "tmdb-123", "tt123",
                2023, "Missing metadata"
        );

        // Then
        assertEquals(MediaType.MOVIE, item.mediaType());
        assertEquals("Broken Movie", item.title());
        assertEquals("jf-123", item.jellyfinId());
        assertEquals("tmdb-123", item.TMDbId());
        assertEquals("tt123", item.IMDbId());
        assertEquals(2023, item.productionYear());
        assertEquals("Missing metadata", item.missingFields());
        assertFalse(item.resolved());
    }

    @Test
    void constructorWithResolved_shouldSetResolvedFlag() {
        // Given & When
        BrokenMediaItem item = new BrokenMediaItem(
                MediaType.SERIES, "Broken Series", "jf-456", null, null,
                2024, "Missing TMDb ID", true
        );

        // Then
        assertTrue(item.resolved());
    }

    @Test
    void setters_shouldUpdateValues() {
        // Given
        BrokenMediaItem item = new BrokenMediaItem(
                MediaType.MOVIE, "Original", "jf-1", null, null,
                2023, "Fields"
        );

        // When
        item.setTitle("Updated");
        item.setResolved(true);
        item.setProductionYear(2024);

        // Then
        assertEquals("Updated", item.title());
        assertTrue(item.resolved());
        assertEquals(2024, item.productionYear());
    }
}
