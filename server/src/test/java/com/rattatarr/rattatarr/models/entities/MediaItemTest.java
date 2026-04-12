package com.rattatarr.rattatarr.models.entities;

import com.rattatarr.rattatarr.models.MediaType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MediaItemTest {

    @Test
    void constructor_shouldCreateMediaItem() {
        // Given & When
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", "jf-123", "tmdb-123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        // Then
        assertEquals(MediaType.MOVIE, mediaItem.mediaType());
        assertEquals("Test Movie", mediaItem.title());
        assertEquals("jf-123", mediaItem.jellyfinId());
        assertEquals("tmdb-123", mediaItem.TMDbId());
        assertEquals("tt123", mediaItem.IMDbId());
        assertEquals(2023, mediaItem.productionYear());
        assertEquals(120, mediaItem.runtimeMinutes());
        assertNotNull(mediaItem.genres());
        assertNotNull(mediaItem.seasons());
    }

    @Test
    void setters_shouldUpdateValues() {
        // Given
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Original", "jf-1", "tmdb-1", "tt1",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        // When
        mediaItem.setTitle("Updated Title");
        mediaItem.setProductionYear(2024);
        mediaItem.setRuntimeMinutes(130);
        mediaItem.setIMDbId("tt9999999");

        // Then
        assertEquals("Updated Title", mediaItem.title());
        assertEquals(2024, mediaItem.productionYear());
        assertEquals(130, mediaItem.runtimeMinutes());
        assertEquals("tt9999999", mediaItem.IMDbId());
    }

    @Test
    void constructor_shouldHandleNullValues() {
        // Given & When
        MediaItem mediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, null, null,
                null, null, Set.of(), Set.of(), Set.of(), Set.of()
        );

        // Then
        assertEquals("Test Series", mediaItem.title());
        assertNull(mediaItem.jellyfinId());
        assertNull(mediaItem.TMDbId());
        assertNull(mediaItem.IMDbId());
    }

    @Test
    void setIMDbId_shouldUpdateIMDbId() {
        // Given
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", "jf-1", "tmdb-1", null,
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
        assertNull(mediaItem.IMDbId());

        // When
        mediaItem.setIMDbId("tt1234567");

        // Then
        assertEquals("tt1234567", mediaItem.IMDbId());
    }

    @Test
    void setIMDbId_shouldAllowNull() {
        // Given
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", "jf-1", "tmdb-1", "tt1234567",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
        assertEquals("tt1234567", mediaItem.IMDbId());

        // When
        mediaItem.setIMDbId(null);

        // Then
        assertNull(mediaItem.IMDbId());
    }

    @Test
    void constructor_shouldHandleDifferentIMDbIdFormats() {
        // Given & When - standard IMDb ID format
        MediaItem movie = new MediaItem(
                MediaType.MOVIE, "Movie", "jf-1", "tmdb-1", "tt0111161",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        // Given & When - longer IMDb ID
        MediaItem series = new MediaItem(
                MediaType.SERIES, "Series", "jf-2", "tmdb-2", "tt12345678",
                2023, 45, Set.of(), Set.of(), Set.of(), Set.of()
        );

        // Then
        assertEquals("tt0111161", movie.IMDbId());
        assertEquals("tt12345678", series.IMDbId());
    }
}
