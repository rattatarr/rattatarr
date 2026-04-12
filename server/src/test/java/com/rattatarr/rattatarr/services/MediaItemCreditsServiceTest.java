package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.TMDbClient;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbCreditsResponseDTO;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.repositories.MediaItemsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class MediaItemCreditsServiceTest {

    @Mock
    private MediaItemsRepository mediaItemsRepository;
    @Mock
    private TMDbClient tmdbClient;
    @Mock
    private PeopleService peopleService;
    @Mock
    private Executor tmdbApiExecutor;

    @InjectMocks
    private MediaItemCreditsService service;

    private MediaItem movieItem;
    private MediaItem seriesItem;
    private TMDbCreditsResponseDTO creditsResponse;

    @BeforeEach
    void setUp() {
        movieItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, null, Set.of(), Set.of(), Set.of(), Set.of()
        );
        seriesItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "456", "tt456",
                2023, null, Set.of(), Set.of(), Set.of(), Set.of()
        );
        creditsResponse = new TMDbCreditsResponseDTO(List.of(), List.of());

        // Mock executor to run tasks synchronously in tests (lenient to avoid UnnecessaryStubbingException)
        lenient().doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(tmdbApiExecutor).execute(any());
    }

    @Test
    void updateAllMediaItemCredits_shouldFetchMovieCredits() {
        when(mediaItemsRepository.findAll(ArgumentMatchers.<Specification<MediaItem>>any()))
                .thenReturn(List.of(movieItem));
        when(tmdbClient.findMovieCreditsById(eq("123"))).thenReturn(creditsResponse);

        service.updateAllMediaItemCredits(false);

        verify(tmdbClient).findMovieCreditsById(eq("123"));
        verify(peopleService).upsertPeopleFromCredits(eq(creditsResponse), eq(movieItem), eq(false));
    }

    @Test
    void updateAllMediaItemCredits_shouldFetchSeriesCredits() {
        when(mediaItemsRepository.findAll(ArgumentMatchers.<Specification<MediaItem>>any()))
                .thenReturn(List.of(seriesItem));
        when(tmdbClient.findTVShowCreditsById(eq("456"))).thenReturn(creditsResponse);

        service.updateAllMediaItemCredits(false);

        verify(tmdbClient).findTVShowCreditsById(eq("456"));
        verify(peopleService).upsertPeopleFromCredits(eq(creditsResponse), eq(seriesItem), eq(false));
    }

    @Test
    void updateAllMediaItemCredits_shouldHandleMultipleMediaItems() {
        when(mediaItemsRepository.findAll(ArgumentMatchers.<Specification<MediaItem>>any()))
                .thenReturn(List.of(movieItem, seriesItem));
        when(tmdbClient.findMovieCreditsById(eq("123"))).thenReturn(creditsResponse);
        when(tmdbClient.findTVShowCreditsById(eq("456"))).thenReturn(creditsResponse);

        service.updateAllMediaItemCredits(false);

        verify(tmdbClient).findMovieCreditsById(eq("123"));
        verify(tmdbClient).findTVShowCreditsById(eq("456"));
        verify(peopleService, times(2)).upsertPeopleFromCredits(any(), any(), eq(false));
    }

    @Test
    void updateAllMediaItemCredits_shouldHandleEmptyList() {
        when(mediaItemsRepository.findAll(ArgumentMatchers.<Specification<MediaItem>>any()))
                .thenReturn(List.of());

        service.updateAllMediaItemCredits(false);

        verify(tmdbClient, never()).findMovieCreditsById(any());
        verify(tmdbClient, never()).findTVShowCreditsById(any());
        verify(peopleService, never()).upsertPeopleFromCredits(any(), any(), any());
    }

    @Test
    void updateAllMediaItemCredits_shouldPassForceRefreshFlag() {
        when(mediaItemsRepository.findAll(ArgumentMatchers.<Specification<MediaItem>>any()))
                .thenReturn(List.of(movieItem));
        when(tmdbClient.findMovieCreditsById(eq("123"))).thenReturn(creditsResponse);

        service.updateAllMediaItemCredits(true);

        verify(peopleService).upsertPeopleFromCredits(eq(creditsResponse), eq(movieItem), eq(true));
    }

    @Test
    void triggerBackgroundCreditsUpdate_shouldNotThrow() {
        when(mediaItemsRepository.findAll(ArgumentMatchers.<Specification<MediaItem>>any()))
                .thenReturn(List.of());

        // Fire-and-forget; just verify no exception
        service.triggerBackgroundCreditsUpdate(false);

        try { Thread.sleep(50); } catch (InterruptedException ignored) {}
    }
}
