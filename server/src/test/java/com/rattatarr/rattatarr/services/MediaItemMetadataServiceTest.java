package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.TMDbClient;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbMovieResponseDTO;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbShowResponseDTO;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemMetadata;
import com.rattatarr.rattatarr.repositories.MediaItemMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaItemMetadataServiceTest {

    @Mock
    private MediaItemMetadataRepository repository;
    @Mock
    private TMDbClient tmDbClient;
    @Mock
    private MediaSeasonMetadataService mediaSeasonMetadataService;
    @Mock
    private MediaItemsService mediaItemsService;

    @InjectMocks
    private MediaItemMetadataService mediaItemMetadataService;

    private MediaItem movieItem;
    private MediaItem seriesItem;
    private MediaItemMetadata metadata;

    @BeforeEach
    void setUp() {
        movieItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        seriesItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "456", "tt456",
                2023, null, Set.of(), Set.of(), Set.of(), Set.of()
        );

        metadata = new MediaItemMetadata(movieItem, "Overview", "/poster.jpg", "/backdrop.jpg");
    }

    @Test
    void upsert_shouldSaveNewMetadataWhenNotExists() {
        // Given
        when(repository.findByMediaItemId(any())).thenReturn(Optional.empty());
        when(repository.save(any(MediaItemMetadata.class))).thenReturn(metadata);

        // When
        MediaItemMetadata result = mediaItemMetadataService.upsert(metadata, false);

        // Then
        assertNotNull(result);
        verify(repository).findByMediaItemId(any());
        verify(repository).save(metadata);
    }

    @Test
    void upsert_shouldReturnExistingWhenForceRefreshFalse() {
        // Given
        MediaItemMetadata existing = new MediaItemMetadata(movieItem, "Old overview", "/old.jpg", "/old_backdrop.jpg");
        when(repository.findByMediaItemId(any())).thenReturn(Optional.of(existing));

        // When
        MediaItemMetadata result = mediaItemMetadataService.upsert(metadata, false);

        // Then
        assertNotNull(result);
        verify(repository).findByMediaItemId(any());
        verify(repository, never()).save(any());
    }

    @Test
    void upsert_shouldUpdateExistingWhenForceRefreshTrue() {
        // Given
        MediaItemMetadata existing = new MediaItemMetadata(movieItem, "Old overview", "/old.jpg", "/old_backdrop.jpg");
        when(repository.findByMediaItemId(any())).thenReturn(Optional.of(existing));
        when(repository.save(any(MediaItemMetadata.class))).thenReturn(existing);

        // When
        MediaItemMetadata result = mediaItemMetadataService.upsert(metadata, true);

        // Then
        assertNotNull(result);
        verify(repository).findByMediaItemId(any());
        verify(repository).save(existing);
    }

    @Test
    void enrichFromTMDb_movie_shouldEnrichSuccessfully() {
        // Given
        TMDbMovieResponseDTO movieDetails = new TMDbMovieResponseDTO(
                "Movie Overview", "/poster.jpg", "/backdrop.jpg"
        );

        when(tmDbClient.findMovieById("123")).thenReturn(movieDetails);
        when(repository.findByMediaItemId(any())).thenReturn(Optional.empty());
        when(repository.save(any(MediaItemMetadata.class))).thenReturn(metadata);

        // When
        MediaItemMetadata result = mediaItemMetadataService.enrichFromTMDb(movieItem, false);

        // Then
        assertNotNull(result);
        verify(tmDbClient).findMovieById("123");
        verify(repository).save(any(MediaItemMetadata.class));
    }

    @Test
    void enrichFromTMDb_series_shouldEnrichSuccessfully() {
        // Given
        TMDbShowResponseDTO showDetails = new TMDbShowResponseDTO(
                "Series Overview", "/poster.jpg", "/backdrop.jpg", null
        );

        when(tmDbClient.findTVShowById("456")).thenReturn(showDetails);
        when(repository.findByMediaItemId(any())).thenReturn(Optional.empty());
        when(repository.save(any(MediaItemMetadata.class))).thenReturn(metadata);

        // When
        MediaItemMetadata result = mediaItemMetadataService.enrichFromTMDb(seriesItem, false);

        // Then
        assertNotNull(result);
        verify(tmDbClient).findTVShowById("456");
        verify(repository).save(any(MediaItemMetadata.class));
        verify(mediaSeasonMetadataService).upsertBatchFromTMDbShow(eq(seriesItem), eq(showDetails), eq(false));
    }

    @Test
    void updateEntity_shouldDoNothing() {
        // Given
        MediaItemMetadata existing = new MediaItemMetadata(movieItem, "Old", "/old.jpg", "/old_backdrop.jpg");
        MediaItemMetadata updated = new MediaItemMetadata(movieItem, "New", "/new.jpg", "/new_backdrop.jpg");

        // When
        mediaItemMetadataService.updateEntity(existing, updated);

        // Then - no exception thrown, method is empty
        assertEquals("Old", existing.description());
    }
}
