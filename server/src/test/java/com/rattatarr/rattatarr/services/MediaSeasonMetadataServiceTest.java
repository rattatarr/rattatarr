package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.TMDbClient;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbSeasonResponseDTO;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbSeasonWithEpisodesResponseDTO;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbShowResponseDTO;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaSeason;
import com.rattatarr.rattatarr.models.entities.MediaSeasonMetadata;
import com.rattatarr.rattatarr.repositories.MediaSeasonMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaSeasonMetadataServiceTest {

    @Mock
    private MediaSeasonMetadataRepository repository;

    @Mock
    private TMDbClient tmDbClient;

    @Mock
    private MediaSeasonsService mediaSeasonsService;

    @InjectMocks
    private MediaSeasonMetadataService service;

    private MediaItem mediaItem;
    private MediaSeason mediaSeason;
    private MediaSeasonMetadata seasonMetadata;

    @BeforeEach
    void setUp() {
        mediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, null, Set.of(), Set.of(), Set.of(), Set.of()
        );
        mediaSeason = new MediaSeason(mediaItem, "jf-123", 1, "Season 1", Set.of());
        seasonMetadata = new MediaSeasonMetadata(mediaSeason, 10, "/poster.jpg", "2023-01-01");
    }

    @Test
    void updateEntity_shouldUpdateAllFields() {
        // Given
        MediaSeasonMetadata existing = new MediaSeasonMetadata(mediaSeason, 5, "/old.jpg", "2022-01-01");
        MediaSeasonMetadata updated = new MediaSeasonMetadata(mediaSeason, 10, "/new.jpg", "2023-01-01");

        // When
        service.updateEntity(existing, updated);

        // Then
        assertEquals(10, existing.episodeCount());
        assertEquals("/new.jpg", existing.posterImageUrl());
        assertEquals("2023-01-01", existing.airDate());
    }

    @Test
    void findByMediaSeasonId_shouldReturnMetadata() {
        // Given
        UUID seasonId = UUID.randomUUID();
        when(repository.findByMediaSeasonId(seasonId)).thenReturn(Optional.of(seasonMetadata));

        // When
        Optional<MediaSeasonMetadata> result = service.findByMediaSeasonId(seasonId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(seasonMetadata, result.get());
        verify(repository).findByMediaSeasonId(seasonId);
    }

    @Test
    void findByMediaSeasonId_shouldReturnEmptyWhenNotFound() {
        // Given
        UUID seasonId = UUID.randomUUID();
        when(repository.findByMediaSeasonId(seasonId)).thenReturn(Optional.empty());

        // When
        Optional<MediaSeasonMetadata> result = service.findByMediaSeasonId(seasonId);

        // Then
        assertTrue(result.isEmpty());
        verify(repository).findByMediaSeasonId(seasonId);
    }

    @Test
    void upsertWithRefresh_shouldSkipUpdateWhenExistsAndForceRefreshFalse() {
        // Given
        MediaSeasonMetadata existing = new MediaSeasonMetadata(mediaSeason, 5, "/old.jpg", "2022-01-01");
        when(repository.findByMediaSeasonId(any())).thenReturn(Optional.of(existing));

        // When
        MediaSeasonMetadata result = service.upsertWithRefresh(seasonMetadata, false);

        // Then
        assertEquals(existing, result);
        verify(repository, never()).save(any());
    }

    @Test
    void upsertWithRefresh_shouldUpdateWhenExistsAndForceRefreshTrue() {
        // Given
        MediaSeasonMetadata existing = new MediaSeasonMetadata(mediaSeason, 5, "/old.jpg", "2022-01-01");
        when(repository.findByMediaSeasonId(any())).thenReturn(Optional.of(existing));
        when(repository.save(any(MediaSeasonMetadata.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaSeasonMetadata result = service.upsertWithRefresh(seasonMetadata, true);

        // Then
        assertNotNull(result);
        verify(repository).save(any(MediaSeasonMetadata.class));
    }

    @Test
    void upsertWithRefresh_shouldCreateWhenNotExists() {
        // Given
        when(repository.findByMediaSeasonId(any())).thenReturn(Optional.empty());
        when(repository.save(any(MediaSeasonMetadata.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaSeasonMetadata result = service.upsertWithRefresh(seasonMetadata, false);

        // Then
        assertNotNull(result);
        verify(repository).save(any(MediaSeasonMetadata.class));
    }

    @Test
    void upsertBatchFromTMDbShow_shouldHandleEmptySeasons() {
        // Given
        TMDbShowResponseDTO showResponse = new TMDbShowResponseDTO(
                "Overview", "/poster.jpg", "/backdrop.jpg", List.of()
        );

        // When
        service.upsertBatchFromTMDbShow(mediaItem, showResponse, false);

        // Then
        verify(mediaSeasonsService, never()).findByMediaItemAndSeason(any(), anyInt());
    }

    @Test
    void upsertBatchFromTMDbShow_shouldUpsertSeasonMetadata() {
        // Given
        TMDbSeasonResponseDTO seasonDTO = new TMDbSeasonResponseDTO(10, "/poster.jpg", "2023-01-01", 1, "Season 1");
        TMDbShowResponseDTO showResponse = new TMDbShowResponseDTO(
                "Overview", "/poster.jpg", "/backdrop.jpg", List.of(seasonDTO)
        );

        when(mediaSeasonsService.findByMediaItemAndSeason(eq(mediaItem), eq(1)))
                .thenReturn(Optional.of(mediaSeason));
        when(repository.findByMediaSeasonId(any())).thenReturn(Optional.empty());
        when(repository.save(any(MediaSeasonMetadata.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.upsertBatchFromTMDbShow(mediaItem, showResponse, false);

        // Then
        verify(mediaSeasonsService).findByMediaItemAndSeason(eq(mediaItem), eq(1));
        verify(repository).save(any(MediaSeasonMetadata.class));
    }

    @Test
    void upsertBatchFromTMDbShow_shouldSkipWhenSeasonNotFound() {
        // Given
        TMDbSeasonResponseDTO seasonDTO = new TMDbSeasonResponseDTO(10, "/poster.jpg", "2023-01-01", 1, "Season 1");
        TMDbShowResponseDTO showResponse = new TMDbShowResponseDTO(
                "Overview", "/poster.jpg", "/backdrop.jpg", List.of(seasonDTO)
        );

        when(mediaSeasonsService.findByMediaItemAndSeason(eq(mediaItem), eq(1)))
                .thenReturn(Optional.empty());

        // When
        service.upsertBatchFromTMDbShow(mediaItem, showResponse, false);

        // Then
        verify(mediaSeasonsService).findByMediaItemAndSeason(eq(mediaItem), eq(1));
        verify(repository, never()).save(any());
    }

    @Test
    void upsertBatchFromSeasonDetails_shouldHandleEmptyMap() {
        // Given
        Map<Integer, TMDbSeasonWithEpisodesResponseDTO> emptyMap = Map.of();

        // When
        service.upsertBatchFromSeasonDetails(emptyMap, mediaItem, false);

        // Then
        verify(mediaSeasonsService, never()).findByMediaItemAndSeason(any(), anyInt());
    }

    @Test
    void upsertBatchFromSeasonDetails_shouldUpsertSeasonMetadata() {
        // Given
        TMDbSeasonWithEpisodesResponseDTO seasonDetails = new TMDbSeasonWithEpisodesResponseDTO(
                "season-id-1", "Season 1", "Overview", "/poster.jpg", "2023-01-01", 1, 8.5, List.of()
        );
        Map<Integer, TMDbSeasonWithEpisodesResponseDTO> detailsMap = Map.of(1, seasonDetails);

        when(mediaSeasonsService.findByMediaItemAndSeason(mediaItem, 1)).thenReturn(Optional.of(mediaSeason));
        when(repository.findByMediaSeasonId(any())).thenReturn(Optional.empty());
        when(repository.save(any(MediaSeasonMetadata.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.upsertBatchFromSeasonDetails(detailsMap, mediaItem, false);

        // Then
        verify(mediaSeasonsService).findByMediaItemAndSeason(mediaItem, 1);
        verify(repository).save(any(MediaSeasonMetadata.class));
    }

    @Test
    void upsertBatchFromSeasonDetails_shouldSkipWhenSeasonNotFound() {
        // Given
        TMDbSeasonWithEpisodesResponseDTO seasonDetails = new TMDbSeasonWithEpisodesResponseDTO(
                "season-id-1", "Season 1", "Overview", "/poster.jpg", "2023-01-01", 1, 8.5, List.of()
        );
        Map<Integer, TMDbSeasonWithEpisodesResponseDTO> detailsMap = Map.of(1, seasonDetails);

        when(mediaSeasonsService.findByMediaItemAndSeason(mediaItem, 1)).thenReturn(Optional.empty());

        // When
        service.upsertBatchFromSeasonDetails(detailsMap, mediaItem, false);

        // Then
        verify(mediaSeasonsService).findByMediaItemAndSeason(mediaItem, 1);
        verify(repository, never()).save(any());
    }

    @Test
    void upsertBatchFromSeasonDetails_shouldSkipNullSeasonData() {
        // Given
        Map<Integer, TMDbSeasonWithEpisodesResponseDTO> detailsMap = new HashMap<>();
        detailsMap.put(1, null);

        // When
        service.upsertBatchFromSeasonDetails(detailsMap, mediaItem, false);

        // Then
        verify(mediaSeasonsService, never()).findByMediaItemAndSeason(any(), anyInt());
        verify(repository, never()).save(any());
    }
}
