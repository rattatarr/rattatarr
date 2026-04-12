package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.repositories.MediaItemsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaItemsServiceTest {

    @Mock
    private MediaItemsRepository repository;

    @InjectMocks
    private MediaItemsService service;

    private MediaItem testMediaItem;

    @BeforeEach
    void setUp() {
        testMediaItem = new MediaItem(
                MediaType.MOVIE,
                "Test Movie",
                "jf-123",
                "tmdb-123",
                "imdb-123",
                2024,
                120,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );
    }

    @Test
    void testFindByJellyfinId_Found() {
        // Given
        when(repository.findByJellyfinId("jf-123")).thenReturn(Optional.of(testMediaItem));

        // When
        Optional<MediaItem> result = service.findByJellyfinId("jf-123");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Movie", result.get().title());
        verify(repository).findByJellyfinId("jf-123");
    }

    @Test
    void testFindByJellyfinId_NotFound() {
        // Given
        when(repository.findByJellyfinId("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<MediaItem> result = service.findByJellyfinId("nonexistent");

        // Then
        assertFalse(result.isPresent());
        verify(repository).findByJellyfinId("nonexistent");
    }

    @Test
    void testFindByTMDbId_Found() {
        // Given
        when(repository.findByTMDbId("tmdb-123")).thenReturn(Optional.of(testMediaItem));

        // When
        Optional<MediaItem> result = service.findByTMDbId("tmdb-123");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Movie", result.get().title());
        verify(repository).findByTMDbId("tmdb-123");
    }

    @Test
    void testFindByTMDbId_NotFound() {
        // Given
        when(repository.findByTMDbId("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<MediaItem> result = service.findByTMDbId("nonexistent");

        // Then
        assertFalse(result.isPresent());
        verify(repository).findByTMDbId("nonexistent");
    }

    @Test
    void testFindAllWithGenres() {
        // Given
        MediaItem item2 = new MediaItem(
                MediaType.SERIES,
                "Test Series",
                "jf-456",
                "tmdb-456",
                "imdb-456",
                2024,
                45,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );
        when(repository.findAllWithGenres()).thenReturn(List.of(testMediaItem, item2));

        // When
        List<MediaItem> result = service.findAllWithGenres();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository).findAllWithGenres();
    }

    @Test
    void testFindByJellyfinId() {
        // Given
        when(repository.findByJellyfinId("jf-123")).thenReturn(Optional.of(testMediaItem));

        // When
        Optional<MediaItem> result = service.findByJellyfinId("jf-123");

        // Then
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals("Test Movie", result.get().title());
        verify(repository).findByJellyfinId("jf-123");
    }

    @Test
    void testUpdateEntity() {
        // Given
        MediaItem existing = new MediaItem(
                MediaType.MOVIE,
                "Old Title",
                "jf-old",
                "tmdb-old",
                "imdb-old",
                2023,
                100,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );

        MediaItem updated = new MediaItem(
                MediaType.MOVIE,
                "New Title",
                "jf-new",
                "tmdb-new",
                "imdb-new",
                2024,
                120,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );

        // When
        service.updateEntity(existing, updated);

        // Then
        assertEquals("New Title", existing.title());
        assertEquals("jf-new", existing.jellyfinId());
        assertEquals("tmdb-new", existing.TMDbId());
        assertEquals("imdb-new", existing.IMDbId());
    }

    @Test
    void testFindStaleSeries() {
        // Given
        Duration threshold = Duration.ofDays(2);
        MediaItem staleSeries1 = new MediaItem(
                MediaType.SERIES,
                "Stale Series 1",
                null,
                "tmdb-111",
                "imdb-111",
                2022,
                45,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );
        MediaItem staleSeries2 = new MediaItem(
                MediaType.SERIES,
                "Stale Series 2",
                null,
                "tmdb-222",
                "imdb-222",
                2022,
                45,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );

        when(repository.findAll(any(Specification.class)))
                .thenReturn(List.of(staleSeries1, staleSeries2));

        // When
        List<MediaItem> result = service.findStaleSeries(threshold);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Stale Series 1", result.get(0).title());
        assertEquals("Stale Series 2", result.get(1).title());
        verify(repository).findAll(any(Specification.class));
    }
}
