package com.rattatarr.rattatarr.models.dtos.responses;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaSeason;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SeasonResponseDTOTest {

    @Test
    void fromEntity_shouldConvertSeasonToDTO() {
        // Given
        MediaItem series = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaSeason season = new MediaSeason(series, "jf-s1", 1, "Season 1", Set.of());

        // When
        SeasonResponseDTO dto = SeasonResponseDTO.fromEntity(season);

        // Then
        assertNotNull(dto);
        assertEquals(1, dto.season());
        assertEquals("Season 1", dto.title());
        assertEquals("jf-s1", dto.jellyfinId());
    }

    @Test
    void fromEntities_shouldConvertMultipleSeasons() {
        // Given
        MediaItem series = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaSeason season1 = new MediaSeason(series, "jf-s1", 1, "Season 1", Set.of());
        MediaSeason season2 = new MediaSeason(series, "jf-s2", 2, "Season 2", Set.of());
        Set<MediaSeason> seasons = Set.of(season1, season2);

        // When
        var dtos = SeasonResponseDTO.fromEntities(seasons);

        // Then
        assertEquals(2, dtos.size());
    }
}
