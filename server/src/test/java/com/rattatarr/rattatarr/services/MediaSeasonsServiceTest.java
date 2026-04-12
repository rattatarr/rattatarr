package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbSeasonResponseDTO;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaSeason;
import com.rattatarr.rattatarr.repositories.MediaSeasonsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaSeasonsServiceTest {

    @Mock
    private MediaSeasonsRepository repository;

    @InjectMocks
    private MediaSeasonsService mediaSeasonsService;

    private MediaItem mediaItem;
    private MediaSeason season;

    @BeforeEach
    void setUp() {
        mediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "456", "tt456",
                2023, null, Set.of(), Set.of(), Set.of(), Set.of()
        );
        season = new MediaSeason(mediaItem, null, 1, "Season 1", Set.of());
    }

    @Test
    void updateEntity_shouldUpdateAllFields() {
        // Given
        MediaSeason existing = new MediaSeason(mediaItem, null, 1, "Old Title", Set.of());
        MediaSeason updated = new MediaSeason(mediaItem, "jf-123", 2, "New Title", Set.of());

        // When
        mediaSeasonsService.updateEntity(existing, updated);

        // Then
        assertEquals("New Title", existing.title());
        assertEquals(2, existing.season());
        assertEquals("jf-123", existing.jellyfinId());
    }

    @Test
    void findByMediaItemAndSeason_shouldReturnSeason() {
        // Given
        when(repository.findByMediaItemAndSeason(mediaItem, 1)).thenReturn(Optional.of(season));

        // When
        Optional<MediaSeason> result = mediaSeasonsService.findByMediaItemAndSeason(mediaItem, 1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(season, result.get());
        verify(repository).findByMediaItemAndSeason(mediaItem, 1);
    }

    @Test
    void upsertBatchFromTMDb_shouldHandleEmptyList() {
        // Given
        List<TMDbSeasonResponseDTO> emptyList = List.of();

        // When
        mediaSeasonsService.upsertBatchFromTMDb(emptyList, mediaItem);

        // Then
        verify(repository, never()).findByMediaItemAndSeason(any(), anyInt());
    }

    @Test
    void upsertBatchFromTMDb_shouldUpsertSeasons() {
        // Given
        TMDbSeasonResponseDTO dto1 = new TMDbSeasonResponseDTO(10, "/poster1.jpg", "2023-01-01", 1, "Season 1");
        TMDbSeasonResponseDTO dto2 = new TMDbSeasonResponseDTO(10, "/poster2.jpg", "2023-01-01", 2, "Season 2");
        List<TMDbSeasonResponseDTO> dtos = List.of(dto1, dto2);

        when(repository.findByMediaItemAndSeason(eq(mediaItem), anyInt())).thenReturn(Optional.empty());
        when(repository.save(any(MediaSeason.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        mediaSeasonsService.upsertBatchFromTMDb(dtos, mediaItem);

        // Then
        verify(repository, times(2)).findByMediaItemAndSeason(eq(mediaItem), anyInt());
        verify(repository, times(2)).save(any(MediaSeason.class));
    }
}
