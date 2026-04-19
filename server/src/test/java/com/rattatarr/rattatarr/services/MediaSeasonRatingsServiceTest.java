package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaSeason;
import com.rattatarr.rattatarr.models.entities.MediaSeasonRating;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.repositories.MediaSeasonRatingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaSeasonRatingsServiceTest {

    @Mock
    private MediaSeasonRatingsRepository repository;

    @InjectMocks
    private MediaSeasonRatingsService service;

    @Test
    void findByProfileAndMediaSeason_withExistingRating_shouldReturnRating() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaSeason mediaSeason = new MediaSeason(mediaItem, null, 1, null, Set.of());
        MediaSeasonRating expectedRating = new MediaSeasonRating(profile, mediaSeason, 8.5f);

        when(repository.findByProfileAndMediaSeason(eq(profile), eq(mediaSeason)))
                .thenReturn(Optional.of(expectedRating));

        // When
        Optional<MediaSeasonRating> result = service.findByProfileAndMediaSeason(profile, mediaSeason);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedRating, result.get());
        verify(repository).findByProfileAndMediaSeason(eq(profile), eq(mediaSeason));
    }

    @Test
    void findByProfileAndMediaSeason_withNonExistentRating_shouldReturnEmpty() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaSeason mediaSeason = new MediaSeason(mediaItem, null, 1, null, Set.of());

        when(repository.findByProfileAndMediaSeason(eq(profile), eq(mediaSeason)))
                .thenReturn(Optional.empty());

        // When
        Optional<MediaSeasonRating> result = service.findByProfileAndMediaSeason(profile, mediaSeason);

        // Then
        assertFalse(result.isPresent());
        verify(repository).findByProfileAndMediaSeason(eq(profile), eq(mediaSeason));
    }

    @Test
    void upsert_withNewRating_shouldCreateNewRating() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaSeason mediaSeason = new MediaSeason(mediaItem, null, 1, null, Set.of());
        Float rating = 9.0f;

        MediaSeasonRating savedRating = new MediaSeasonRating(profile, mediaSeason, rating);

        when(repository.findByProfileAndMediaSeason(eq(profile), eq(mediaSeason)))
                .thenReturn(Optional.empty());
        when(repository.save(any(MediaSeasonRating.class)))
                .thenReturn(savedRating);

        // When
        MediaSeasonRating result = service.upsert(profile, mediaSeason, rating);

        // Then
        assertNotNull(result);
        assertEquals(rating, result.rating());
        assertEquals(profile, result.profile());
        assertEquals(mediaSeason, result.mediaSeason());
        verify(repository).findByProfileAndMediaSeason(eq(profile), eq(mediaSeason));
        verify(repository).save(any(MediaSeasonRating.class));
    }

    @Test
    void upsert_withExistingRating_shouldUpdateRating() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaSeason mediaSeason = new MediaSeason(mediaItem, null, 1, null, Set.of());
        Float oldRating = 7.0f;
        Float newRating = 9.5f;

        MediaSeasonRating existingRating = new MediaSeasonRating(profile, mediaSeason, oldRating);

        when(repository.findByProfileAndMediaSeason(eq(profile), eq(mediaSeason)))
                .thenReturn(Optional.of(existingRating));
        when(repository.save(any(MediaSeasonRating.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaSeasonRating result = service.upsert(profile, mediaSeason, newRating);

        // Then
        assertNotNull(result);
        verify(repository).findByProfileAndMediaSeason(eq(profile), eq(mediaSeason));
        verify(repository).save(any(MediaSeasonRating.class));
    }

    @Test
    void upsert_withMinimumRating_shouldSucceed() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaSeason mediaSeason = new MediaSeason(mediaItem, null, 1, null, Set.of());
        Float rating = 1.0f;

        when(repository.findByProfileAndMediaSeason(eq(profile), eq(mediaSeason)))
                .thenReturn(Optional.empty());
        when(repository.save(any(MediaSeasonRating.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaSeasonRating result = service.upsert(profile, mediaSeason, rating);

        // Then
        assertNotNull(result);
        assertEquals(rating, result.rating());
        verify(repository).save(any(MediaSeasonRating.class));
    }

    @Test
    void upsert_withMaximumRating_shouldSucceed() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaSeason mediaSeason = new MediaSeason(mediaItem, null, 1, null, Set.of());
        Float rating = 10.0f;

        when(repository.findByProfileAndMediaSeason(eq(profile), eq(mediaSeason)))
                .thenReturn(Optional.empty());
        when(repository.save(any(MediaSeasonRating.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaSeasonRating result = service.upsert(profile, mediaSeason, rating);

        // Then
        assertNotNull(result);
        assertEquals(rating, result.rating());
        verify(repository).save(any(MediaSeasonRating.class));
    }

    @Test
    void upsert_withHalfStarRating_shouldSucceed() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaSeason mediaSeason = new MediaSeason(mediaItem, null, 1, null, Set.of());
        Float rating = 7.5f;

        when(repository.findByProfileAndMediaSeason(eq(profile), eq(mediaSeason)))
                .thenReturn(Optional.empty());
        when(repository.save(any(MediaSeasonRating.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaSeasonRating result = service.upsert(profile, mediaSeason, rating);

        // Then
        assertNotNull(result);
        assertEquals(rating, result.rating());
        verify(repository).save(any(MediaSeasonRating.class));
    }

    @Test
    void upsert_withMultipleSeasons_shouldHandleIndependently() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaSeason season1 = new MediaSeason(mediaItem, null, 1, null, Set.of());
        MediaSeason season2 = new MediaSeason(mediaItem, null, 2, null, Set.of());

        when(repository.findByProfileAndMediaSeason(eq(profile), eq(season1)))
                .thenReturn(Optional.empty());
        when(repository.findByProfileAndMediaSeason(eq(profile), eq(season2)))
                .thenReturn(Optional.empty());
        when(repository.save(any(MediaSeasonRating.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MediaSeasonRating rating1 = service.upsert(profile, season1, 8.0f);
        MediaSeasonRating rating2 = service.upsert(profile, season2, 9.5f);

        // Then
        assertNotNull(rating1);
        assertNotNull(rating2);
        assertEquals(8.0f, rating1.rating());
        assertEquals(9.5f, rating2.rating());
        verify(repository, times(2)).save(any(MediaSeasonRating.class));
    }

    @Test
    void updateEntity_shouldUpdateRatingValue() {
        // Given
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaSeason mediaSeason = new MediaSeason(mediaItem, null, 1, null, Set.of());

        MediaSeasonRating existing = new MediaSeasonRating(profile, mediaSeason, 7.0f);
        MediaSeasonRating updated = new MediaSeasonRating(profile, mediaSeason, 9.0f);

        // When
        service.updateEntity(existing, updated);

        // Then
        assertEquals(9.0f, existing.rating());
        assertEquals(profile, existing.profile());
        assertEquals(mediaSeason, existing.mediaSeason());
    }

    @Test
    void batchFetchRatingsMap_withProfileAndSeasonIds_shouldReturnMap() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID seasonId1 = UUID.randomUUID();
        UUID seasonId2 = UUID.randomUUID();

        when(repository.findAllByProfileIdAndMediaSeasonIdIn(eq(profileId), any()))
                .thenReturn(List.of());

        // When
        Map<UUID, Float> result = service.batchFetchRatingsMap(List.of(seasonId1, seasonId2), profileId);

        // Then
        assertNotNull(result);
        verify(repository).findAllByProfileIdAndMediaSeasonIdIn(eq(profileId), any());
    }

    @Test
    void batchFetchRatingsMap_withNullProfileId_shouldReturnEmptyMap() {
        // When
        Map<UUID, Float> result = service.batchFetchRatingsMap(List.of(UUID.randomUUID()), null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(repository);
    }

    @Test
    void batchFetchRatingsMap_withEmptySeasonIds_shouldReturnEmptyMap() {
        // When
        Map<UUID, Float> result = service.batchFetchRatingsMap(List.of(), UUID.randomUUID());

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(repository);
    }

    @Test
    void updateEntity_shouldNotChangeProfileOrMediaSeason() {
        // Given
        Profile profile1 = new Profile("Profile 1", null);
        Profile profile2 = new Profile("Profile 2", null);
        MediaItem mediaItem1 = new MediaItem(
                MediaType.SERIES, "Series 1", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaItem mediaItem2 = new MediaItem(
                MediaType.SERIES, "Series 2", null, "456", "tt456",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaSeason season1 = new MediaSeason(mediaItem1, null, 1, null, Set.of());
        MediaSeason season2 = new MediaSeason(mediaItem2, null, 1, null, Set.of());

        MediaSeasonRating existing = new MediaSeasonRating(profile1, season1, 7.0f);
        MediaSeasonRating updated = new MediaSeasonRating(profile2, season2, 9.0f);

        // When
        service.updateEntity(existing, updated);

        // Then
        assertEquals(9.0f, existing.rating());
        assertEquals(profile1, existing.profile()); // Should not change
        assertEquals(season1, existing.mediaSeason()); // Should not change
    }
}
