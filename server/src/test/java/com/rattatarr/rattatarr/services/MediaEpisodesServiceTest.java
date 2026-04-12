package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbEpisodeResponseDTO;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaEpisode;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaSeason;
import com.rattatarr.rattatarr.repositories.MediaEpisodesRepository;
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
class MediaEpisodesServiceTest {

    @Mock
    private MediaEpisodesRepository repository;

    @InjectMocks
    private MediaEpisodesService mediaEpisodesService;

    private MediaItem mediaItem;
    private MediaSeason mediaSeason;
    private MediaEpisode episode;

    @BeforeEach
    void setUp() {
        mediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "456", "tt456",
                2023, null, Set.of(), Set.of(), Set.of(), Set.of()
        );
        mediaSeason = new MediaSeason(mediaItem, null, 1, "Season 1", Set.of());
        episode = new MediaEpisode(mediaSeason, 1, "Pilot", 45);
    }

    @Test
    void updateEntity_shouldUpdateAllFields() {
        // Given
        MediaEpisode existing = new MediaEpisode(mediaSeason, 1, "Old Title", 30);
        MediaEpisode updated = new MediaEpisode(mediaSeason, 2, "New Title", 45);
        updated.setJellyfinId("jf-123");

        // When
        mediaEpisodesService.updateEntity(existing, updated);

        // Then
        assertEquals("New Title", existing.title());
        assertEquals(2, existing.episode());
        assertEquals(45, existing.runtimeMinutes());
        assertEquals("jf-123", existing.jellyfinId());
    }

    @Test
    void findByMediaSeasonAndEpisode_shouldReturnEpisode() {
        // Given
        when(repository.findByMediaSeasonAndEpisode(mediaSeason, 1)).thenReturn(Optional.of(episode));

        // When
        Optional<MediaEpisode> result = mediaEpisodesService.findByMediaSeasonAndEpisode(mediaSeason, 1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(episode, result.get());
        verify(repository).findByMediaSeasonAndEpisode(mediaSeason, 1);
    }

    @Test
    void upsertBatchFromTMDb_shouldHandleEmptyList() {
        // Given
        List<TMDbEpisodeResponseDTO> emptyList = List.of();

        // When
        mediaEpisodesService.upsertBatchFromTMDb(emptyList, mediaSeason);

        // Then
        verify(repository, never()).findByMediaSeasonAndEpisode(any(), anyInt());
    }

    @Test
    void upsertBatchFromTMDb_shouldUpsertEpisodes() {
        // Given
        TMDbEpisodeResponseDTO dto1 = new TMDbEpisodeResponseDTO(
                101, "Episode 1", "Overview", 1, 1, "2023-01-01", 45, "/still.jpg", 8.5, 100
        );
        TMDbEpisodeResponseDTO dto2 = new TMDbEpisodeResponseDTO(
                102, "Episode 2", "Overview", 2, 1, "2023-01-08", 50, "/still2.jpg", 8.7, 150
        );
        List<TMDbEpisodeResponseDTO> dtos = List.of(dto1, dto2);

        when(repository.findByMediaSeasonAndEpisode(eq(mediaSeason), anyInt())).thenReturn(Optional.empty());
        when(repository.save(any(MediaEpisode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        mediaEpisodesService.upsertBatchFromTMDb(dtos, mediaSeason);

        // Then
        verify(repository, times(2)).findByMediaSeasonAndEpisode(eq(mediaSeason), anyInt());
        verify(repository, times(2)).save(any(MediaEpisode.class));
    }
}
