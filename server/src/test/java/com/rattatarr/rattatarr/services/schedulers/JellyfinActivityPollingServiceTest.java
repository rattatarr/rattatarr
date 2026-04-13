package com.rattatarr.rattatarr.services.schedulers;

import com.rattatarr.rattatarr.clients.jellyfin.JellyfinClient;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientActivityLogEntryResponseDTO;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientPlaybackItemResponseDTO;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientPlaybackItemUserDataResponseDTO;
import com.rattatarr.rattatarr.clients.jellyfin.responses.wrappers.JellyfinClientActivityLogEntriesWrapper;
import com.rattatarr.rattatarr.models.WatchEventType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.models.entities.WatchEvent;
import com.rattatarr.rattatarr.repositories.WatchEventsRepository;
import com.rattatarr.rattatarr.services.MediaEpisodesService;
import com.rattatarr.rattatarr.services.MediaItemsService;
import com.rattatarr.rattatarr.services.ProfilesService;
import com.rattatarr.rattatarr.services.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.Set;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JellyfinActivityPollingServiceTest {

    @Mock
    private JellyfinClient jellyfinClient;
    @Mock
    private SettingsService settingsService;
    @Mock
    private ProfilesService profilesService;
    @Mock
    private MediaItemsService mediaItemsService;
    @Mock
    private MediaEpisodesService mediaEpisodesService;
    @Mock
    private WatchEventsRepository watchEventsRepository;

    @InjectMocks
    private JellyfinActivityPollingService service;

    @BeforeEach
    void setUp() {
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_JELLYFIN_ENABLED), eq(true))).thenReturn(true);
        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_JELLYFIN_ACTIVITY_BACKFILL_COMPLETE), eq(false))).thenReturn(true);
        when(jellyfinClient.isConfigured()).thenReturn(true);
    }

    @Test
    void pollJellyfinActivity_shouldSkipWhenSyncDisabled() {
        doReturn(false).when(settingsService).getBooleanSetting(anyString(), anyBoolean());

        service.pollJellyfinActivity();

        verify(jellyfinClient, never()).getActivityLogEntries();
        verify(watchEventsRepository, never()).saveAll(any());
    }

    @Test
    void pollJellyfinActivity_shouldCreateStartEventForVideoPlayback() {
        Profile profile = new Profile("Alice", "user-1");
        MediaItem mediaItem = mock(MediaItem.class);

        JellyfinClientActivityLogEntryResponseDTO entry = new JellyfinClientActivityLogEntryResponseDTO(
                100L,
                "Playback started",
                null,
                null,
                "VideoPlayback",
                "movie-1",
                "2026-04-13T09:20:36.680Z",
                "user-1",
                "Trace"
        );

        when(jellyfinClient.getActivityLogEntries())
                .thenReturn(new JellyfinClientActivityLogEntriesWrapper(List.of(entry), 1, 0));
        when(profilesService.findByJellyfinId("user-1")).thenReturn(Optional.of(profile));
        when(mediaItemsService.findByJellyfinId("movie-1")).thenReturn(Optional.of(mediaItem));

        service.pollJellyfinActivity();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WatchEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(watchEventsRepository).saveAll(eventCaptor.capture());
        WatchEvent saved = eventCaptor.getValue().getFirst();

        assertEquals(WatchEventType.START, saved.eventType());
        assertEquals(profile, saved.profile());
        assertEquals(mediaItem, saved.mediaItem());
        assertEquals(100L, saved.jellyfinLogId());
        assertNotNull(saved.watchedAt());
        assertEquals(Instant.parse("2026-04-13T09:20:36.680Z"), saved.watchedAt());
    }

    @Test
    void pollJellyfinActivity_shouldCreateCompleteEventForVideoPlaybackStoppedWhenPlayedTrue() {
        Profile profile = new Profile("Alice", "user-1");
        MediaItem mediaItem = mock(MediaItem.class);

        JellyfinClientActivityLogEntryResponseDTO entry = new JellyfinClientActivityLogEntryResponseDTO(
                101L,
                "Playback stopped",
                null,
                null,
                "VideoPlaybackStopped",
                "movie-1",
                "2026-04-13T09:25:36.680Z",
                "user-1",
                "Trace"
        );

        when(jellyfinClient.getActivityLogEntries())
                .thenReturn(new JellyfinClientActivityLogEntriesWrapper(List.of(entry), 1, 0));
        when(profilesService.findByJellyfinId("user-1")).thenReturn(Optional.of(profile));
        when(mediaItemsService.findByJellyfinId("movie-1")).thenReturn(Optional.of(mediaItem));
        when(jellyfinClient.getItemByIdForUser("movie-1", "user-1")).thenReturn(new JellyfinClientPlaybackItemResponseDTO(
                "movie-1",
                "Movie",
                "Inception",
                new JellyfinClientPlaybackItemUserDataResponseDTO(1_000_000_000L, true)
        ));

        service.pollJellyfinActivity();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WatchEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(watchEventsRepository).saveAll(eventCaptor.capture());
        WatchEvent saved = eventCaptor.getValue().getFirst();

        assertEquals(WatchEventType.COMPLETE, saved.eventType());
        assertEquals(100, saved.positionSeconds());
        verify(jellyfinClient, times(1)).getItemByIdForUser("movie-1", "user-1");
    }

    @Test
    void pollJellyfinActivity_shouldCreateProgressEventForVideoPlaybackStoppedWhenPlayedFalse() {
        Profile profile = new Profile("Alice", "user-1");
        MediaItem mediaItem = mock(MediaItem.class);

        JellyfinClientActivityLogEntryResponseDTO entry = new JellyfinClientActivityLogEntryResponseDTO(
                102L,
                "Playback stopped",
                null,
                null,
                "VideoPlaybackStopped",
                "movie-1",
                "2026-04-13T09:30:36.680Z",
                "user-1",
                "Trace"
        );

        when(jellyfinClient.getActivityLogEntries())
                .thenReturn(new JellyfinClientActivityLogEntriesWrapper(List.of(entry), 1, 0));
        when(profilesService.findByJellyfinId("user-1")).thenReturn(Optional.of(profile));
        when(mediaItemsService.findByJellyfinId("movie-1")).thenReturn(Optional.of(mediaItem));
        when(jellyfinClient.getItemByIdForUser("movie-1", "user-1")).thenReturn(new JellyfinClientPlaybackItemResponseDTO(
                "movie-1",
                "Movie",
                "Inception",
                new JellyfinClientPlaybackItemUserDataResponseDTO(450_000_000L, false)
        ));

        service.pollJellyfinActivity();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<WatchEvent>> eventCaptor = ArgumentCaptor.forClass(List.class);
        verify(watchEventsRepository).saveAll(eventCaptor.capture());
        WatchEvent saved = eventCaptor.getValue().getFirst();

        assertEquals(WatchEventType.PROGRESS, saved.eventType());
        assertEquals(45, saved.positionSeconds());
    }

    @Test
    void pollJellyfinActivity_shouldAvoidDuplicateProcessingByActivityId() {
        Profile profile = new Profile("Alice", "user-1");
        MediaItem mediaItem = mock(MediaItem.class);

        JellyfinClientActivityLogEntryResponseDTO entry = new JellyfinClientActivityLogEntryResponseDTO(
                103L,
                "Playback started",
                null,
                null,
                "VideoPlayback",
                "movie-1",
                "2026-04-13T09:35:36.680Z",
                "user-1",
                "Trace"
        );

        when(jellyfinClient.getActivityLogEntries())
                .thenReturn(new JellyfinClientActivityLogEntriesWrapper(List.of(entry), 1, 0));
        when(profilesService.findByJellyfinId("user-1")).thenReturn(Optional.of(profile));
        when(mediaItemsService.findByJellyfinId("movie-1")).thenReturn(Optional.of(mediaItem));

        when(watchEventsRepository.findExistingJellyfinLogIds(Set.of(103L)))
                .thenReturn(Set.of())
                .thenReturn(Set.of(103L));

        service.pollJellyfinActivity();
        service.pollJellyfinActivity();

        verify(watchEventsRepository, times(1)).saveAll(any());
    }

    @Test
    void pollJellyfinActivity_shouldSkipEntryWhenJellyfinLogIdAlreadyExists() {
        JellyfinClientActivityLogEntryResponseDTO entry = new JellyfinClientActivityLogEntryResponseDTO(
                200L,
                "Playback started",
                null,
                null,
                "VideoPlayback",
                "movie-1",
                "2026-04-13T10:00:36.680Z",
                "user-1",
                "Trace"
        );

        when(jellyfinClient.getActivityLogEntries())
                .thenReturn(new JellyfinClientActivityLogEntriesWrapper(List.of(entry), 1, 0));
        when(watchEventsRepository.findExistingJellyfinLogIds(Set.of(200L))).thenReturn(Set.of(200L));

        service.pollJellyfinActivity();

        verify(watchEventsRepository, never()).saveAll(any());
        verify(profilesService, never()).findByJellyfinId(anyString());
    }

    @Test
    void pollJellyfinActivity_shouldQueryExistingLogIdsInBatch() {
        Profile profile = new Profile("Alice", "user-1");
        MediaItem mediaItem = mock(MediaItem.class);

        JellyfinClientActivityLogEntryResponseDTO first = new JellyfinClientActivityLogEntryResponseDTO(
                301L,
                "Playback started",
                null,
                null,
                "VideoPlayback",
                "movie-1",
                "2026-04-13T10:10:36.680Z",
                "user-1",
                "Trace"
        );
        JellyfinClientActivityLogEntryResponseDTO second = new JellyfinClientActivityLogEntryResponseDTO(
                302L,
                "Playback started",
                null,
                null,
                "VideoPlayback",
                "movie-2",
                "2026-04-13T10:11:36.680Z",
                "user-1",
                "Trace"
        );

        when(jellyfinClient.getActivityLogEntries())
                .thenReturn(new JellyfinClientActivityLogEntriesWrapper(List.of(first, second), 2, 0));
        when(watchEventsRepository.findExistingJellyfinLogIds(Set.of(301L, 302L))).thenReturn(Set.of(301L));
        when(profilesService.findByJellyfinId("user-1")).thenReturn(Optional.of(profile));
        when(mediaItemsService.findByJellyfinId("movie-2")).thenReturn(Optional.of(mediaItem));

        service.pollJellyfinActivity();

        verify(watchEventsRepository).findExistingJellyfinLogIds(Set.of(301L, 302L));
        verify(watchEventsRepository, times(1)).saveAll(any());
    }

    @Test
    void pollJellyfinActivity_shouldRunInitialBackfillWhenLogIdOneNotPresent() {
        Profile profile = new Profile("Alice", "user-1");
        MediaItem mediaItem = mock(MediaItem.class);

        JellyfinClientActivityLogEntryResponseDTO older = new JellyfinClientActivityLogEntryResponseDTO(
                2L,
                "Playback started",
                null,
                null,
                "VideoPlayback",
                "movie-1",
                "2026-04-13T10:10:36.680Z",
                "user-1",
                "Trace"
        );
        JellyfinClientActivityLogEntryResponseDTO oldest = new JellyfinClientActivityLogEntryResponseDTO(
                1L,
                "Playback started",
                null,
                null,
                "VideoPlayback",
                "movie-1",
                "2026-04-13T10:09:36.680Z",
                "user-1",
                "Trace"
        );

        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_JELLYFIN_ACTIVITY_BACKFILL_COMPLETE), eq(false))).thenReturn(false);
        when(jellyfinClient.getActivityLogEntries(0))
                .thenReturn(new JellyfinClientActivityLogEntriesWrapper(List.of(older), 2, 0));
        when(jellyfinClient.getActivityLogEntries(50))
                .thenReturn(new JellyfinClientActivityLogEntriesWrapper(List.of(oldest), 2, 50));
        when(watchEventsRepository.findExistingJellyfinLogIds(Set.of(2L, 1L))).thenReturn(Set.of());
        when(profilesService.findByJellyfinId("user-1")).thenReturn(Optional.of(profile));
        when(mediaItemsService.findByJellyfinId("movie-1")).thenReturn(Optional.of(mediaItem));

        service.pollJellyfinActivity();

        verify(jellyfinClient).getActivityLogEntries(0);
        verify(jellyfinClient).getActivityLogEntries(50);
        verify(settingsService).updateSetting(SettingsService.SYNC_JELLYFIN_ACTIVITY_BACKFILL_COMPLETE, "true", null);
        verify(watchEventsRepository, times(1)).saveAll(any());
    }

    @Test
    void pollJellyfinActivity_shouldSkipInitialBackfillWhenBackfillFlagAlreadySet() {
        JellyfinClientActivityLogEntryResponseDTO entry = new JellyfinClientActivityLogEntryResponseDTO(
                500L,
                "Playback started",
                null,
                null,
                "VideoPlayback",
                "movie-1",
                "2026-04-13T10:20:36.680Z",
                "user-1",
                "Trace"
        );

        when(settingsService.getBooleanSetting(eq(SettingsService.SYNC_JELLYFIN_ACTIVITY_BACKFILL_COMPLETE), eq(false))).thenReturn(true);
        when(jellyfinClient.getActivityLogEntries())
                .thenReturn(new JellyfinClientActivityLogEntriesWrapper(List.of(entry), 1, 0));
        when(watchEventsRepository.findExistingJellyfinLogIds(Set.of(500L))).thenReturn(Set.of());
        when(profilesService.findByJellyfinId("user-1")).thenReturn(Optional.of(new Profile("Alice", "user-1")));
        when(mediaItemsService.findByJellyfinId("movie-1")).thenReturn(Optional.of(mock(MediaItem.class)));

        service.pollJellyfinActivity();

        verify(jellyfinClient).getActivityLogEntries();
        verify(jellyfinClient, never()).getActivityLogEntries(50);
        verify(settingsService, never()).updateSetting(eq(SettingsService.SYNC_JELLYFIN_ACTIVITY_BACKFILL_COMPLETE), any(), any());
    }
}
