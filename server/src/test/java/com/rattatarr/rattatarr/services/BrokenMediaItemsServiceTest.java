package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.BrokenMediaItemsExceptions;
import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.exceptions.MediaItemExceptions;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.dtos.requests.BrokenMediaItemsFiltersDTO;
import com.rattatarr.rattatarr.models.entities.BrokenMediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.repositories.BrokenMediaItemsRepository;
import com.rattatarr.rattatarr.repositories.MediaItemsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrokenMediaItemsServiceTest {

    @Mock
    private BrokenMediaItemsRepository repository;

    @Mock
    private MediaItemsRepository mediaItemsRepository;

    @InjectMocks
    private BrokenMediaItemsService service;

    private BrokenMediaItem testBrokenItem;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testBrokenItem = new BrokenMediaItem(
                MediaType.MOVIE,
                "Test Movie",
                "jf-123",
                null,
                null,
                2024,
                "Missing TMDb ID"
        );
        pageable = PageRequest.of(0, 20);
    }

    @Test
    void testFindByJellyfinId_Found() {
        // Given
        when(repository.findByJellyfinId("jf-123")).thenReturn(Optional.of(testBrokenItem));

        // When
        Optional<BrokenMediaItem> result = service.findByJellyfinId("jf-123");

        // Then
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals("jf-123", result.get().jellyfinId());
        verify(repository).findByJellyfinId("jf-123");
    }

    @Test
    void testFindByJellyfinId_NotFound() {
        // Given
        when(repository.findByJellyfinId("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<BrokenMediaItem> result = service.findByJellyfinId("nonexistent");

        // Then
        assertNotNull(result);
        assertFalse(result.isPresent());
        verify(repository).findByJellyfinId("nonexistent");
    }

    @Test
    void testSaveBrokenMediaItem() {
        // Given / When
        service.saveBrokenMediaItem(testBrokenItem);

        // Then
        verify(repository).save(testBrokenItem);
    }

    @Test
    void testFilterBrokenMediaItems_Movies() {
        // Given
        BrokenMediaItemsFiltersDTO filters = new BrokenMediaItemsFiltersDTO(MediaType.MOVIE);
        Page<BrokenMediaItem> page = new PageImpl<>(List.of(testBrokenItem), pageable, 1);

        when(repository.findAll(ArgumentMatchers.<Specification<BrokenMediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);

        // When
        Page<BrokenMediaItem> result = service.filterBrokenMediaItems(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAll(ArgumentMatchers.<Specification<BrokenMediaItem>>any(), eq(pageable));
    }

    @Test
    void testFilterBrokenMediaItems_Series() {
        // Given
        BrokenMediaItem seriesItem = new BrokenMediaItem(
                MediaType.SERIES,
                "Test Series",
                "jf-789",
                null,
                null,
                2024,
                "Missing TMDb ID"
        );
        BrokenMediaItemsFiltersDTO filters = new BrokenMediaItemsFiltersDTO(MediaType.SERIES);
        Page<BrokenMediaItem> page = new PageImpl<>(List.of(seriesItem), pageable, 1);

        when(repository.findAll(ArgumentMatchers.<Specification<BrokenMediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);

        // When
        Page<BrokenMediaItem> result = service.filterBrokenMediaItems(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAll(ArgumentMatchers.<Specification<BrokenMediaItem>>any(), eq(pageable));
    }

    @Test
    void testFilterBrokenMediaItems_EmptyResult() {
        // Given
        BrokenMediaItemsFiltersDTO filters = new BrokenMediaItemsFiltersDTO(MediaType.MOVIE);
        Page<BrokenMediaItem> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(repository.findAll(ArgumentMatchers.<Specification<BrokenMediaItem>>any(), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        Page<BrokenMediaItem> result = service.filterBrokenMediaItems(filters, pageable);

        // Then
        assertTrue(result.isEmpty());
        verify(repository).findAll(ArgumentMatchers.<Specification<BrokenMediaItem>>any(), any(Pageable.class));
    }

    @Test
    void resolveById_shouldLinkMediaItemAndMarkResolved() {
        // Given
        UUID brokenId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        BrokenMediaItem item = new BrokenMediaItem(
                MediaType.MOVIE, "Test Movie", "jf-123",
                null, null, 2020, "TMDbId"
        );
        MediaItem mediaItem = mock(MediaItem.class);
        when(repository.findById(brokenId)).thenReturn(Optional.of(item));
        when(mediaItemsRepository.findById(mediaItemId)).thenReturn(Optional.of(mediaItem));
        when(repository.save(any(BrokenMediaItem.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        BrokenMediaItem result = service.resolveById(brokenId, mediaItemId);

        // Then
        assertTrue(result.resolved());
        assertEquals(mediaItem, result.resolvedMediaItem());
        verify(repository).save(item);
        verify(repository).findById(brokenId);
        verify(mediaItemsRepository).findById(mediaItemId);
    }

    @Test
    void resolveById_shouldThrowWhenBrokenItemNotFound() {
        // Given
        UUID brokenId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        when(repository.findById(brokenId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(
                BrokenMediaItemsExceptions.BrokenMediaItemNotFoundExceptions.class,
                () -> service.resolveById(brokenId, mediaItemId)
        );
        verify(repository, never()).save(any());
    }

    @Test
    void resolveById_shouldThrowWhenMediaItemNotFound() {
        // Given
        UUID brokenId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        BrokenMediaItem item = new BrokenMediaItem(
                MediaType.MOVIE, "Test Movie", "jf-123",
                null, null, 2020, "TMDbId"
        );
        when(repository.findById(brokenId)).thenReturn(Optional.of(item));
        when(mediaItemsRepository.findById(mediaItemId)).thenReturn(Optional.empty());

        // When / Then
        assertThrows(
                MediaItemExceptions.MediaItemNotFoundExceptions.class,
                () -> service.resolveById(brokenId, mediaItemId)
        );
        verify(repository, never()).save(any());
    }

    @Test
    void resolveById_shouldThrowWhenMediaItemIdIsNull() {
        // Given
        UUID brokenId = UUID.randomUUID();

        // When / Then
        assertThrows(
                CommonExceptions.InvalidRequestExceptions.class,
                () -> service.resolveById(brokenId, null)
        );
        verify(repository, never()).findById(any());
        verify(repository, never()).save(any());
    }
}
