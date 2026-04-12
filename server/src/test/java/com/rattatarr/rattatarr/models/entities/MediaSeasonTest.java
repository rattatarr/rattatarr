package com.rattatarr.rattatarr.models.entities;

import com.rattatarr.rattatarr.models.MediaType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MediaSeasonTest {

    @Test
    void constructor_shouldCreateMediaSeason() {
        // Given
        MediaItem series = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );

        // When
        MediaSeason season = new MediaSeason(series, "jf-s1", 1, "Season 1", Set.of());

        // Then
        assertEquals(series, season.mediaItem());
        assertEquals("jf-s1", season.jellyfinId());
        assertEquals(1, season.season());
        assertEquals("Season 1", season.title());
        assertNotNull(season.episodes());
    }

    @Test
    void setters_shouldUpdateValues() {
        // Given
        MediaItem series = new MediaItem(
                MediaType.SERIES, "Test", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaSeason season = new MediaSeason(series, "jf-1", 1, "Season 1", Set.of());

        // When
        season.setTitle("Updated Season 1");
        season.setSeason(2);

        // Then
        assertEquals("Updated Season 1", season.title());
        assertEquals(2, season.season());
    }
}
