package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemRating;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.repositories.MediaItemRatingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaItemRatingsServiceTest {

    @Mock
    private MediaItemRatingsRepository repository;

    @InjectMocks
    private MediaItemRatingsService service;

    @Test
    void findByProfileAndMediaItem_withExistingRating_shouldReturnRating() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaItemRating expectedRating = new MediaItemRating(profile, mediaItem, 8.5f);

        when(repository.findByProfileAndMediaItem(eq(profile), eq(mediaItem)))
                .thenReturn(Optional.of(expectedRating));

        // When
        Optional<MediaItemRating> result = service.findByProfileAndMediaItem(profile, mediaItem);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedRating, result.get());
        verify(repository).findByProfileAndMediaItem(eq(profile), eq(mediaItem));
    }

    @Test
    void findByProfileAndMediaItem_withNonExistentRating_shouldReturnEmpty() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        when(repository.findByProfileAndMediaItem(eq(profile), eq(mediaItem)))
                .thenReturn(Optional.empty());

        // When
        Optional<MediaItemRating> result = service.findByProfileAndMediaItem(profile, mediaItem);

        // Then
        assertFalse(result.isPresent());
        verify(repository).findByProfileAndMediaItem(eq(profile), eq(mediaItem));
    }

    @Test
    void upsert_withNewRating_shouldCreateNewRating() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
        Float rating = 9.0f;

        MediaItemRating savedRating = new MediaItemRating(profile, mediaItem, rating);

        when(repository.findByProfileAndMediaItem(eq(profile), eq(mediaItem)))
                .thenReturn(Optional.empty());
        when(repository.save(any(MediaItemRating.class)))
                .thenReturn(savedRating);

        // When
        MediaItemRating result = service.upsert(profile, mediaItem, rating);

        // Then
        assertNotNull(result);
        assertEquals(rating, result.rating());
        assertEquals(profile, result.profile());
        assertEquals(mediaItem, result.mediaItem());
        verify(repository).findByProfileAndMediaItem(eq(profile), eq(mediaItem));
        verify(repository).save(any(MediaItemRating.class));
    }

    @Test
    void upsert_withExistingRating_shouldUpdateRating() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
        Float oldRating = 7.0f;
        Float newRating = 9.5f;

        MediaItemRating existingRating = new MediaItemRating(profile, mediaItem, oldRating);

        when(repository.findByProfileAndMediaItem(eq(profile), eq(mediaItem)))
                .thenReturn(Optional.of(existingRating));
        when(repository.save(any(MediaItemRating.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaItemRating result = service.upsert(profile, mediaItem, newRating);

        // Then
        assertNotNull(result);
        verify(repository).findByProfileAndMediaItem(eq(profile), eq(mediaItem));
        verify(repository).save(any(MediaItemRating.class));
    }

    @Test
    void upsert_withMinimumRating_shouldSucceed() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
        Float rating = 1.0f;

        when(repository.findByProfileAndMediaItem(eq(profile), eq(mediaItem)))
                .thenReturn(Optional.empty());
        when(repository.save(any(MediaItemRating.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaItemRating result = service.upsert(profile, mediaItem, rating);

        // Then
        assertNotNull(result);
        assertEquals(rating, result.rating());
        verify(repository).save(any(MediaItemRating.class));
    }

    @Test
    void upsert_withMaximumRating_shouldSucceed() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
        Float rating = 10.0f;

        when(repository.findByProfileAndMediaItem(eq(profile), eq(mediaItem)))
                .thenReturn(Optional.empty());
        when(repository.save(any(MediaItemRating.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaItemRating result = service.upsert(profile, mediaItem, rating);

        // Then
        assertNotNull(result);
        assertEquals(rating, result.rating());
        verify(repository).save(any(MediaItemRating.class));
    }

    @Test
    void upsert_withHalfStarRating_shouldSucceed() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
        Float rating = 7.5f;

        when(repository.findByProfileAndMediaItem(eq(profile), eq(mediaItem)))
                .thenReturn(Optional.empty());
        when(repository.save(any(MediaItemRating.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaItemRating result = service.upsert(profile, mediaItem, rating);

        // Then
        assertNotNull(result);
        assertEquals(rating, result.rating());
        verify(repository).save(any(MediaItemRating.class));
    }

    @Test
    void updateEntity_shouldUpdateRatingValue() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        MediaItemRating existing = new MediaItemRating(profile, mediaItem, 7.0f);
        MediaItemRating updated = new MediaItemRating(profile, mediaItem, 9.0f);

        // When
        service.updateEntity(existing, updated);

        // Then
        assertEquals(9.0f, existing.rating());
        assertEquals(profile, existing.profile());
        assertEquals(mediaItem, existing.mediaItem());
    }

    @Test
    void updateEntity_shouldNotChangeProfileOrMediaItem() {
        // Given
        Profile profile1 = new Profile("Profile 1", null);
        Profile profile2 = new Profile("Profile 2", null);
        MediaItem mediaItem1 = new MediaItem(
                MediaType.MOVIE, "Movie 1", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaItem mediaItem2 = new MediaItem(
                MediaType.MOVIE, "Movie 2", null, "456", "tt456",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        MediaItemRating existing = new MediaItemRating(profile1, mediaItem1, 7.0f);
        MediaItemRating updated = new MediaItemRating(profile2, mediaItem2, 9.0f);

        // When
        service.updateEntity(existing, updated);

        // Then
        assertEquals(9.0f, existing.rating());
        assertEquals(profile1, existing.profile()); // Should not change
        assertEquals(mediaItem1, existing.mediaItem()); // Should not change
    }

    @Test
    void upsert_withRatedAtTimestamp_shouldPreserveTimestamp() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
        Float rating = 8.0f;
        Instant customTimestamp = Instant.now().minus(7, ChronoUnit.DAYS);

        when(repository.findByProfileAndMediaItem(eq(profile), eq(mediaItem)))
                .thenReturn(Optional.empty());
        when(repository.save(any(MediaItemRating.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaItemRating result = service.upsert(profile, mediaItem, rating, customTimestamp);

        // Then
        assertNotNull(result);
        assertEquals(rating, result.rating());
        assertEquals(customTimestamp, result.ratedAt());
        verify(repository).findByProfileAndMediaItem(eq(profile), eq(mediaItem));
        verify(repository).save(any(MediaItemRating.class));
    }

    @Test
    void upsert_withRatedAtTimestamp_andExistingRating_shouldUpdateWithTimestamp() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
        Float oldRating = 6.0f;
        Float newRating = 9.0f;
        Instant oldTimestamp = Instant.now().minus(30, ChronoUnit.DAYS);
        Instant newTimestamp = Instant.now().minus(5, ChronoUnit.DAYS);

        MediaItemRating existingRating = new MediaItemRating(profile, mediaItem, oldRating, oldTimestamp);

        when(repository.findByProfileAndMediaItem(eq(profile), eq(mediaItem)))
                .thenReturn(Optional.of(existingRating));
        when(repository.save(any(MediaItemRating.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaItemRating result = service.upsert(profile, mediaItem, newRating, newTimestamp);

        // Then
        assertNotNull(result);
        assertEquals(newRating, result.rating());
        assertEquals(newTimestamp, result.ratedAt());
        verify(repository).findByProfileAndMediaItem(eq(profile), eq(mediaItem));
        verify(repository).save(any(MediaItemRating.class));
    }
}
