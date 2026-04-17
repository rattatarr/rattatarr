package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.RatedItemSummary;
import com.rattatarr.rattatarr.models.WatchEventType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.repositories.WatchEventsRepository;
import com.rattatarr.rattatarr.specifications.StatisticsSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentPromptServiceTest {

    @Mock
    private MediaItemRatingsService mediaItemRatingsService;
    @Mock
    private WatchEventsRepository watchEventsRepository;

    private AgentPromptService service;

    private final UUID profileId = UUID.randomUUID();
    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = new Profile("Bob", null);
        service = new AgentPromptService(mediaItemRatingsService, watchEventsRepository);
    }

    private void stubEmptyPersonAndGenre(boolean hasEnoughRatings) {
        if (hasEnoughRatings) {
            when(mediaItemRatingsService.findTopGenresByWatchCount(eq(profileId), anyInt())).thenReturn(List.of());
            when(mediaItemRatingsService.findTopGenresByAverageRating(eq(profileId), anyFloat(), anyInt())).thenReturn(List.of());
        } else {
            when(mediaItemRatingsService.findJellyfinTopGenresByCount(eq(profileId), anyInt())).thenReturn(List.of());
        }
        when(mediaItemRatingsService.findTopActors(eq(profileId), anyInt(), anyInt(), any(StatisticsSpecifications.SortBy.class))).thenReturn(List.of());
        when(mediaItemRatingsService.findTopDirectors(eq(profileId), anyInt(), anyInt(), any(StatisticsSpecifications.SortBy.class))).thenReturn(List.of());
        when(mediaItemRatingsService.findTopProducers(eq(profileId), anyInt(), anyInt(), any(StatisticsSpecifications.SortBy.class))).thenReturn(List.of());
    }

    @Test
    void buildSystemPrompt_containsProfileName() {
        when(mediaItemRatingsService.findAllRatedByProfileRecentFirst(profile)).thenReturn(List.of());
        when(mediaItemRatingsService.findTop15ByProfileOrderByRatingDesc(profile)).thenReturn(List.of());
        when(watchEventsRepository.findRecentWatchedMediaItems(eq(profileId), eq(WatchEventType.COMPLETE), any(Pageable.class))).thenReturn(List.of());
        stubEmptyPersonAndGenre(false);

        String prompt = service.buildSystemPrompt(profile, profileId);

        assertTrue(prompt.contains("Bob"));
    }

    @Test
    void buildSystemPrompt_withEnoughRatings_doesNotCallWatchEvents() {
        List<RatedItemSummary> rated = List.of(
                new RatedItemSummary("Film A", "MOVIE", 8.0f),
                new RatedItemSummary("Film B", "MOVIE", 7.0f),
                new RatedItemSummary("Film C", "MOVIE", 9.0f),
                new RatedItemSummary("Film D", "MOVIE", 6.0f),
                new RatedItemSummary("Film E", "MOVIE", 5.0f),
                new RatedItemSummary("Film F", "MOVIE", 7.5f),
                new RatedItemSummary("Film G", "MOVIE", 8.5f),
                new RatedItemSummary("Film H", "MOVIE", 6.5f),
                new RatedItemSummary("Film I", "MOVIE", 9.0f),
                new RatedItemSummary("Film J", "MOVIE", 7.0f));
        when(mediaItemRatingsService.findAllRatedByProfileRecentFirst(profile)).thenReturn(rated);
        when(mediaItemRatingsService.findTop15ByProfileOrderByRatingDesc(profile)).thenReturn(List.of());
        stubEmptyPersonAndGenre(true);

        service.buildSystemPrompt(profile, profileId);

        verify(watchEventsRepository, never()).findRecentWatchedMediaItems(any(), any(), any());
    }

    @Test
    void buildSystemPrompt_withFewRatings_appendsRecentWatches() {
        List<RatedItemSummary> rated = List.of(new RatedItemSummary("Film A", "MOVIE", 8.0f));
        when(mediaItemRatingsService.findAllRatedByProfileRecentFirst(profile)).thenReturn(rated);
        when(mediaItemRatingsService.findTop15ByProfileOrderByRatingDesc(profile)).thenReturn(List.of());

        MediaItem recentMovie = new MediaItem(MediaType.MOVIE, "Interstellar", null, null, null, null, null, Set.of(), Set.of(), Set.of(), Set.of());
        when(watchEventsRepository.findRecentWatchedMediaItems(eq(profileId), eq(WatchEventType.COMPLETE), any(Pageable.class)))
                .thenReturn(List.of(recentMovie));
        stubEmptyPersonAndGenre(false);

        String prompt = service.buildSystemPrompt(profile, profileId);

        assertTrue(prompt.contains("recently watched"));
        assertTrue(prompt.contains("Interstellar"));
    }

    @Test
    void buildSystemPrompt_withFewRatings_excludesAlreadyRatedFromWatchFallback() {
        RatedItemSummary alreadyRated = new RatedItemSummary("Interstellar", "MOVIE", 9.0f);
        when(mediaItemRatingsService.findAllRatedByProfileRecentFirst(profile)).thenReturn(List.of(alreadyRated));
        when(mediaItemRatingsService.findTop15ByProfileOrderByRatingDesc(profile)).thenReturn(List.of());

        MediaItem rated = new MediaItem(MediaType.MOVIE, "Interstellar", null, null, null, null, null, Set.of(), Set.of(), Set.of(), Set.of());
        MediaItem unrated = new MediaItem(MediaType.MOVIE, "Parasite", null, null, null, null, null, Set.of(), Set.of(), Set.of(), Set.of());
        when(watchEventsRepository.findRecentWatchedMediaItems(eq(profileId), eq(WatchEventType.COMPLETE), any(Pageable.class)))
                .thenReturn(List.of(rated, unrated));
        stubEmptyPersonAndGenre(false);

        String prompt = service.buildSystemPrompt(profile, profileId);

        // Parasite (unrated) must appear in the recently-watched section
        assertTrue(prompt.contains("Parasite"));
        // Interstellar must NOT appear in the recently-watched fallback (it is already in the exclusion list)
        int sectionStart = prompt.indexOf("recently watched but hasn't rated yet");
        assertTrue(sectionStart >= 0, "recently-watched section should be present");
        assertFalse(prompt.substring(sectionStart).contains("Interstellar"));
    }

    @Test
    void buildSystemPrompt_withFewRatings_noWatchEvents_doesNotAppendSection() {
        when(mediaItemRatingsService.findAllRatedByProfileRecentFirst(profile)).thenReturn(List.of());
        when(mediaItemRatingsService.findTop15ByProfileOrderByRatingDesc(profile)).thenReturn(List.of());
        when(watchEventsRepository.findRecentWatchedMediaItems(eq(profileId), eq(WatchEventType.COMPLETE), any(Pageable.class)))
                .thenReturn(List.of());
        stubEmptyPersonAndGenre(false);

        String prompt = service.buildSystemPrompt(profile, profileId);

        assertFalse(prompt.contains("recently watched"));
    }
}
