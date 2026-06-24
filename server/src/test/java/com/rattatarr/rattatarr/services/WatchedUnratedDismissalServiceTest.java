package com.rattatarr.rattatarr.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.models.entities.WatchedUnratedDismissal;
import com.rattatarr.rattatarr.repositories.WatchedUnratedDismissalsRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WatchedUnratedDismissalServiceTest {

    @Mock
    private WatchedUnratedDismissalsRepository repository;

    @Mock
    private ProfilesService profilesService;

    @Mock
    private MediaItemsService mediaItemsService;

    @InjectMocks
    private WatchedUnratedDismissalService service;

    private Profile profile() {
        return new Profile("Test Profile", null);
    }

    private MediaItem mediaItem() {
        return new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of());
    }

    @Test
    void dismiss_whenNoExistingDismissal_createsNewWithNowTimestamp() {
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Profile profile = profile();
        MediaItem mediaItem = mediaItem();

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaItemsService.findByIdOrThrow(eq(mediaItemId), any())).thenReturn(mediaItem);
        when(repository.findByProfileIdAndMediaItemId(profileId, mediaItemId)).thenReturn(Optional.empty());

        Instant before = Instant.now();
        service.dismiss(profileId, mediaItemId);

        ArgumentCaptor<WatchedUnratedDismissal> captor = ArgumentCaptor.forClass(WatchedUnratedDismissal.class);
        verify(repository).save(captor.capture());
        WatchedUnratedDismissal saved = captor.getValue();
        assertSame(profile, saved.profile());
        assertSame(mediaItem, saved.mediaItem());
        assertNotNull(saved.dismissedAt());
        assertFalse(saved.dismissedAt().isBefore(before));
    }

    @Test
    void dismiss_whenExistingDismissal_bumpsTimestamp() {
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Profile profile = profile();
        MediaItem mediaItem = mediaItem();
        Instant old = Instant.now().minusSeconds(60 * 60 * 24 * 60); // 60 days ago
        WatchedUnratedDismissal existing = new WatchedUnratedDismissal(profile, mediaItem, old);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaItemsService.findByIdOrThrow(eq(mediaItemId), any())).thenReturn(mediaItem);
        when(repository.findByProfileIdAndMediaItemId(profileId, mediaItemId)).thenReturn(Optional.of(existing));

        service.dismiss(profileId, mediaItemId);

        verify(repository).save(existing);
        assertTrue(existing.dismissedAt().isAfter(old));
    }

    @Test
    void restore_whenDismissalExists_deletesIt() {
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        WatchedUnratedDismissal existing = new WatchedUnratedDismissal(profile(), mediaItem(), Instant.now());

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile());
        when(mediaItemsService.findByIdOrThrow(eq(mediaItemId), any())).thenReturn(mediaItem());
        when(repository.findByProfileIdAndMediaItemId(profileId, mediaItemId)).thenReturn(Optional.of(existing));

        service.restore(profileId, mediaItemId);

        verify(repository).delete(existing);
    }

    @Test
    void restore_whenNoDismissal_isNoOp() {
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile());
        when(mediaItemsService.findByIdOrThrow(eq(mediaItemId), any())).thenReturn(mediaItem());
        when(repository.findByProfileIdAndMediaItemId(profileId, mediaItemId)).thenReturn(Optional.empty());

        service.restore(profileId, mediaItemId);

        verify(repository, never()).delete(any(WatchedUnratedDismissal.class));
    }
}
