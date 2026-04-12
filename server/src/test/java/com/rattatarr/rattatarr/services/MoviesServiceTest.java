package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.dtos.requests.MoviesFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.MovieResponseDTO;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.repositories.MediaItemsRepository;
import com.rattatarr.rattatarr.services.helpers.MediaItemViewHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoviesServiceTest {

    @Mock
    private MediaItemsRepository repository;

    @Mock
    private MediaItemViewHelper mediaItemViewHelper;

    @Mock
    private MediaItemRatingsService mediaItemRatingsService;

    @InjectMocks
    private MoviesService service;

    private MediaItem testMovie;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testMovie = new MediaItem(
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

        pageable = PageRequest.of(0, 20);
    }

    @Test
    void testFilterMovies_AllFilters() {
        // Given
        UUID movieId = UUID.randomUUID();
        MoviesFiltersDTO filters = new MoviesFiltersDTO(
                movieId,
                "Test",
                2023,
                2024,
                Set.of("Action"),
                "w500",
                "w1280",
                "w185",
                null,
                null,
                null,
                null,
                null
        );

        Page<MediaItem> page = new PageImpl<>(List.of(testMovie), pageable, 1);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new java.util.HashMap<>());

        // When
        Page<MovieResponseDTO> result = service.filterMovies(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAll(ArgumentMatchers.<Specification<MediaItem>>any(), eq(pageable));
        verify(mediaItemViewHelper).initializeCredits(eq(testMovie), eq(true));
        verify(mediaItemViewHelper).applyImageUrls(eq(testMovie), eq("w500"), eq("w1280"), eq("w185"), eq(false), eq(true));
        verify(mediaItemRatingsService).batchFetchRatingsMap(any(), nullable(UUID.class));
    }

    @Test
    void testFilterMovies_NoIdFilter() {
        // Given
        MoviesFiltersDTO filters = new MoviesFiltersDTO(
                null,
                "Test",
                null,
                null,
                null,
                "w500",
                "w1280",
                "w185",
                null,
                null,
                null,
                null,
                null
        );

        Page<MediaItem> page = new PageImpl<>(List.of(testMovie), pageable, 1);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new java.util.HashMap<>());

        // When
        Page<MovieResponseDTO> result = service.filterMovies(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(mediaItemViewHelper).initializeCredits(eq(testMovie), eq(false));
        verify(mediaItemViewHelper).applyImageUrls(eq(testMovie), eq("w500"), eq("w1280"), eq("w185"), eq(false), eq(false));
    }

    @Test
    void testFilterMovies_EmptyResult() {
        // Given
        MoviesFiltersDTO filters = new MoviesFiltersDTO(
                null, null, null, null, null, null, null, null, null, null, null, null, null
        );

        Page<MediaItem> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(emptyPage);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new java.util.HashMap<>());

        // When
        Page<MovieResponseDTO> result = service.filterMovies(filters, pageable);

        // Then
        assertTrue(result.isEmpty());
        verify(repository).findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class));
        verify(mediaItemViewHelper, never()).initializeCredits(any(), anyBoolean());
        verify(mediaItemViewHelper, never()).applyImageUrls(any(), any(), any(), any(), anyBoolean(), anyBoolean());
    }

    @Test
    void testFilterMovies_WithRatingSortAndProfileId_repositoryReceivesUnsortedPageable() {
        // Given
        UUID profileId = UUID.randomUUID();
        MoviesFiltersDTO filters = new MoviesFiltersDTO(
                null, null, null, null, null, null, null, null, profileId, null, null, null, null
        );
        Pageable sortedByRating = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "ratings"));

        Page<MediaItem> page = new PageImpl<>(List.of(), sortedByRating, 0);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), any())).thenReturn(new java.util.HashMap<>());

        // When
        service.filterMovies(filters, sortedByRating);

        // Then: Spring Data must receive an unsorted pageable so it does not overwrite the
        // ORDER BY that the rating sort spec sets directly on the criteria query.
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(ArgumentMatchers.<Specification<MediaItem>>any(), pageableCaptor.capture());
        assertFalse(pageableCaptor.getValue().getSort().isSorted());
    }

    @Test
    void testFilterMovies_WithRatingSortNoProfileId_repositoryReceivesPageableWithoutRatingsSort() {
        // Given — no profileId, so rating sort cannot be resolved; it should be silently dropped.
        MoviesFiltersDTO filters = new MoviesFiltersDTO(
                null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        Pageable sortedByRating = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "ratings"));

        Page<MediaItem> page = new PageImpl<>(List.of(), sortedByRating, 0);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), any())).thenReturn(new java.util.HashMap<>());

        // When
        service.filterMovies(filters, sortedByRating);

        // Then: "ratings" sort is gone; pageable is not unsorted (could be truly unsorted or
        // have other properties, but must not contain "ratings").
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(ArgumentMatchers.<Specification<MediaItem>>any(), pageableCaptor.capture());
        assertFalse(pageableCaptor.getValue().getSort().stream()
                .anyMatch(o -> o.getProperty().equalsIgnoreCase("ratings")));
    }

    @Test
    void testFilterMovies_WithRatingSortAndOtherSorts_repositoryReceivesUnsortedPageable() {
        // Given — other sorts (productionYear) must be forwarded into the spec's ORDER BY,
        // not kept in the pageable (which must be unsorted).
        UUID profileId = UUID.randomUUID();
        MoviesFiltersDTO filters = new MoviesFiltersDTO(
                null, null, null, null, null, null, null, null, profileId, null, null, null, null
        );
        Pageable mixed = PageRequest.of(0, 20, Sort.by(
                Sort.Order.asc("ratings"),
                Sort.Order.desc("productionYear")
        ));

        Page<MediaItem> page = new PageImpl<>(List.of(), mixed, 0);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), any())).thenReturn(new java.util.HashMap<>());

        // When
        service.filterMovies(filters, mixed);

        // Then
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(ArgumentMatchers.<Specification<MediaItem>>any(), pageableCaptor.capture());
        assertFalse(pageableCaptor.getValue().getSort().isSorted());
    }

    @Test
    void testFilterMovies_WithNonRatingSort_repositoryReceivesOriginalPageable() {
        // Given — no rating sort at all; pageable must pass through unchanged.
        MoviesFiltersDTO filters = new MoviesFiltersDTO(
                null, null, null, null, null, null, null, null, null, null, null, null, null
        );
        Pageable sortedByYear = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "productionYear"));

        Page<MediaItem> page = new PageImpl<>(List.of(), sortedByYear, 0);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), any())).thenReturn(new java.util.HashMap<>());

        // When
        service.filterMovies(filters, sortedByYear);

        // Then
        verify(repository).findAll(ArgumentMatchers.<Specification<MediaItem>>any(), eq(sortedByYear));
    }

    @Test
    void testFilterMovies_MultipleResults() {
        // Given
        MediaItem movie2 = new MediaItem(
                MediaType.MOVIE,
                "Another Movie",
                "jf-456",
                "tmdb-456",
                "imdb-456",
                2024,
                110,
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>(),
                new HashSet<>()
        );

        MoviesFiltersDTO filters = new MoviesFiltersDTO(
                null, null, null, null, null, null, null, null, null, null, null, null, null
        );

        Page<MediaItem> page = new PageImpl<>(List.of(testMovie, movie2), pageable, 2);
        when(repository.findAll(ArgumentMatchers.<Specification<MediaItem>>any(), any(Pageable.class)))
                .thenReturn(page);
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new java.util.HashMap<>());

        // When
        Page<MovieResponseDTO> result = service.filterMovies(filters, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(mediaItemViewHelper, times(2)).initializeCredits(any(MediaItem.class), eq(false));
        verify(mediaItemViewHelper, times(2)).applyImageUrls(any(MediaItem.class), any(), any(), any(), eq(false), eq(false));
    }
}
