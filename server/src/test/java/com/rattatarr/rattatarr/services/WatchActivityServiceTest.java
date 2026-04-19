package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.WatchEventType;
import com.rattatarr.rattatarr.models.dtos.requests.WatchActivityFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.WatchEventResponseDTO;
import com.rattatarr.rattatarr.models.entities.*;
import com.rattatarr.rattatarr.repositories.MediaItemsRepository;
import com.rattatarr.rattatarr.repositories.WatchEventsRepository;
import com.rattatarr.rattatarr.services.helpers.MediaItemViewHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WatchActivityServiceTest {

    @Mock
    private WatchEventsRepository repository;

    @Mock
    private MediaItemsRepository mediaItemsRepository;

    @Mock
    private MediaItemRatingsService mediaItemRatingsService;

    @Mock
    private MediaItemViewHelper mediaItemViewHelper;

    @InjectMocks
    private WatchActivityService service;

    private MediaItem movie;
    private MediaItem series;
    private MediaSeason season;
    private MediaEpisode episode;
    private Profile profile;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        profile = new Profile("Test User", null);

        movie = new MediaItem(
                MediaType.MOVIE, "Test Movie", "jf-movie", "tmdb-movie", "imdb-movie",
                2024, 120, new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>()
        );
        ReflectionTestUtils.setField(movie, "id", UUID.randomUUID());

        series = new MediaItem(
                MediaType.SERIES, "Test Series", "jf-series", "tmdb-series", null,
                2022, 45, new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>()
        );
        ReflectionTestUtils.setField(series, "id", UUID.randomUUID());

        season = new MediaSeason(series, "jf-s1", 1, "Season 1", new HashSet<>());
        ReflectionTestUtils.setField(season, "id", UUID.randomUUID());
        episode = new MediaEpisode(season, 3, "Episode Title", 45);
        ReflectionTestUtils.setField(episode, "id", UUID.randomUUID());

        pageable = PageRequest.of(0, 20);

        when(mediaItemRatingsService.batchFetchRatingsMap(any(), nullable(UUID.class))).thenReturn(new HashMap<>());
        when(mediaItemsRepository.findAll(ArgumentMatchers.<Specification<MediaItem>>any())).thenReturn(List.of());
    }

    @Test
    void filterActivity_returnsMovieDto_whenMovieEvent() {
        UUID profileId = UUID.randomUUID();
        WatchEvent event = new WatchEvent(1L, profile, movie, null, null, WatchEventType.COMPLETE,
                Instant.parse("2026-04-12T20:00:00Z"), null);
        WatchActivityFiltersDTO filters = new WatchActivityFiltersDTO(profileId, null, null, null, "w500", "w1280");

        when(repository.findAll(ArgumentMatchers.<Specification<WatchEvent>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event), pageable, 1));
        when(mediaItemsRepository.findAll(ArgumentMatchers.<Specification<MediaItem>>any()))
                .thenReturn(List.of(movie));

        Page<WatchEventResponseDTO> result = service.filterActivity(filters, pageable);

        assertEquals(1, result.getContent().size());
        WatchEventResponseDTO dto = result.getContent().getFirst();
        assertNotNull(dto.movie());
        assertNull(dto.show());
        assertEquals(WatchEventType.COMPLETE, dto.eventType());
        assertEquals("Test Movie", dto.movie().title());
    }

    @Test
    void filterActivity_returnsShowDto_withSeasonAndEpisode_whenSeriesEvent() {
        UUID profileId = UUID.randomUUID();
        WatchEvent event = new WatchEvent(2L, profile, series, season, episode, WatchEventType.COMPLETE,
                Instant.parse("2026-04-12T21:00:00Z"), null);
        WatchActivityFiltersDTO filters = new WatchActivityFiltersDTO(profileId, null, null, null, null, null);

        when(repository.findAll(ArgumentMatchers.<Specification<WatchEvent>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event), pageable, 1));
        when(mediaItemsRepository.findAll(ArgumentMatchers.<Specification<MediaItem>>any()))
                .thenReturn(List.of(series));

        Page<WatchEventResponseDTO> result = service.filterActivity(filters, pageable);

        WatchEventResponseDTO dto = result.getContent().getFirst();
        assertNull(dto.movie());
        assertNotNull(dto.show());
        assertEquals(1, dto.seasonNumber());
        assertEquals(3, dto.episodeNumber());
        assertEquals("Episode Title", dto.episodeTitle());
    }

    @Test
    void filterActivity_alwaysSortsByWatchedAtDesc() {
        UUID profileId = UUID.randomUUID();
        WatchActivityFiltersDTO filters = new WatchActivityFiltersDTO(profileId, null, null, null, null, null);

        when(repository.findAll(ArgumentMatchers.<Specification<WatchEvent>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        service.filterActivity(filters, PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "watchedAt")));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(ArgumentMatchers.<Specification<WatchEvent>>any(), pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("watchedAt");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void filterActivity_appliesImageUrls_whenMediaItemsLoaded() {
        UUID profileId = UUID.randomUUID();
        WatchEvent event = new WatchEvent(3L, profile, movie, null, null, WatchEventType.PROGRESS,
                Instant.parse("2026-04-12T18:00:00Z"), 3600);
        WatchActivityFiltersDTO filters = new WatchActivityFiltersDTO(profileId, null, null, null, "w500", "original");

        when(repository.findAll(ArgumentMatchers.<Specification<WatchEvent>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event), pageable, 1));
        when(mediaItemsRepository.findAll(ArgumentMatchers.<Specification<MediaItem>>any()))
                .thenReturn(List.of(movie));

        service.filterActivity(filters, pageable);

        verify(mediaItemViewHelper).applyImageUrls(eq(movie), eq("w500"), eq("original"), isNull(), eq(false), eq(false));
    }

    @Test
    void filterActivity_returnsEmpty_whenNoEvents() {
        UUID profileId = UUID.randomUUID();
        WatchActivityFiltersDTO filters = new WatchActivityFiltersDTO(profileId, null, null, null, null, null);

        when(repository.findAll(ArgumentMatchers.<Specification<WatchEvent>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<WatchEventResponseDTO> result = service.filterActivity(filters, pageable);

        assertTrue(result.isEmpty());
        verify(mediaItemViewHelper, never()).applyImageUrls(any(), any(), any(), any(), anyBoolean(), anyBoolean());
        verify(mediaItemRatingsService).batchFetchRatingsMap(eq(List.of()), nullable(UUID.class));
    }

    @Test
    void filterActivity_includesRating_whenProfileHasRated() {
        UUID profileId = UUID.randomUUID();
        WatchEvent event = new WatchEvent(4L, profile, movie, null, null, WatchEventType.COMPLETE,
                Instant.parse("2026-04-10T15:00:00Z"), null);
        WatchActivityFiltersDTO filters = new WatchActivityFiltersDTO(profileId, null, null, null, null, null);

        MediaItemRating rating = new MediaItemRating(profile, movie, 8.5f);
        when(repository.findAll(ArgumentMatchers.<Specification<WatchEvent>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event), pageable, 1));
        when(mediaItemsRepository.findAll(ArgumentMatchers.<Specification<MediaItem>>any()))
                .thenReturn(List.of(movie));
        when(mediaItemRatingsService.batchFetchRatingsMap(any(), eq(profileId)))
                .thenReturn(new java.util.HashMap<>(java.util.Map.of(movie.id(), rating)));

        Page<WatchEventResponseDTO> result = service.filterActivity(filters, pageable);

        assertEquals(8.5f, result.getContent().getFirst().movie().myRating());
    }

    @Test
    void filterActivity_mapsDeduplicatedResultsFromDb() {
        // Dedup happens in SQL via WatchEventSpecifications.deduplicated().
        // Repo returns already-deduped events — service maps them correctly.
        UUID profileId = UUID.randomUUID();
        WatchEvent completeEvent = new WatchEvent(10L, profile, series, season, episode,
                WatchEventType.COMPLETE, Instant.parse("2026-04-12T21:00:00Z"), null);

        WatchActivityFiltersDTO filters = new WatchActivityFiltersDTO(profileId, null, null, null, null, null);

        when(repository.findAll(ArgumentMatchers.<Specification<WatchEvent>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(completeEvent), pageable, 1));
        when(mediaItemsRepository.findAll(ArgumentMatchers.<Specification<MediaItem>>any()))
                .thenReturn(List.of(series));

        Page<WatchEventResponseDTO> result = service.filterActivity(filters, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(WatchEventType.COMPLETE, result.getContent().getFirst().eventType());
    }

    @Test
    void filterActivity_fallsBackToEventMediaItem_whenNotInLoadedMap() {
        UUID profileId = UUID.randomUUID();
        WatchEvent event = new WatchEvent(5L, profile, movie, null, null, WatchEventType.COMPLETE,
                Instant.parse("2026-04-10T10:00:00Z"), null);
        WatchActivityFiltersDTO filters = new WatchActivityFiltersDTO(profileId, null, null, null, null, null);

        when(repository.findAll(ArgumentMatchers.<Specification<WatchEvent>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(event), pageable, 1));
        // mediaItemsRepository returns empty — simulates soft-deleted or missing item
        when(mediaItemsRepository.findAll(ArgumentMatchers.<Specification<MediaItem>>any()))
                .thenReturn(List.of());

        Page<WatchEventResponseDTO> result = service.filterActivity(filters, pageable);

        assertEquals(1, result.getContent().size());
        assertNotNull(result.getContent().getFirst().movie());
    }
}
