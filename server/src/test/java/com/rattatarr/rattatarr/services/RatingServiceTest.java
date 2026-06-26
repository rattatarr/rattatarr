package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.exceptions.MediaItemExceptions;
import com.rattatarr.rattatarr.exceptions.MediaSeasonExceptions;
import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.RatingMediaType;
import com.rattatarr.rattatarr.models.MediaReviewType;
import com.rattatarr.rattatarr.models.dtos.requests.DeleteRateRequestDTO;
import com.rattatarr.rattatarr.models.dtos.requests.DeleteReviewRequestDTO;
import com.rattatarr.rattatarr.models.dtos.requests.RateRequestDTO;
import com.rattatarr.rattatarr.models.dtos.requests.ReviewRequestDTO;
import com.rattatarr.rattatarr.models.entities.*;
import com.rattatarr.rattatarr.repositories.MediaItemCastRepository;
import com.rattatarr.rattatarr.repositories.MediaItemCrewRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private ProfilesService profilesService;

    @Mock
    private MediaItemRatingsService mediaItemRatingsService;

    @Mock
    private MediaSeasonRatingsService mediaSeasonRatingsService;

    @Mock
    private MediaItemsService mediaItemsService;

    @Mock
    private MediaSeasonsService mediaSeasonsService;

    @Mock
    private MediaItemCastRepository mediaItemCastRepository;

    @Mock
    private MediaItemCrewRepository mediaItemCrewRepository;

    @InjectMocks
    private RatingService ratingService;

    @Test
    void rateMediaItem_withValidRating_shouldSucceed() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Float rating = 8.5f;

        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaItemRating savedRating = new MediaItemRating(profile, mediaItem, rating);

        RateRequestDTO request = new RateRequestDTO(profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, rating);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaItemsService.findByIdOrThrow(eq(mediaItemId), any())).thenReturn(mediaItem);
        when(mediaItemRatingsService.upsert(eq(profile), eq(mediaItem), eq(rating))).thenReturn(savedRating);

        // When
        ratingService.rate(request);

        // Then
        verify(profilesService).findByIdOrThrow(eq(profileId), any());
        verify(mediaItemsService).findByIdOrThrow(eq(mediaItemId), any());
        verify(mediaItemRatingsService).upsert(eq(profile), eq(mediaItem), eq(rating));
    }

    @Test
    void rateMediaSeason_withValidRating_shouldSucceed() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaSeasonId = UUID.randomUUID();
        Float rating = 9.0f;

        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaSeason mediaSeason = new MediaSeason(mediaItem, null, 1, null, Set.of());
        MediaSeasonRating savedRating = new MediaSeasonRating(profile, mediaSeason, rating);

        RateRequestDTO request = new RateRequestDTO(profileId, mediaSeasonId, RatingMediaType.MEDIA_SEASON, rating);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaSeasonsService.findByIdOrThrow(eq(mediaSeasonId), any())).thenReturn(mediaSeason);
        when(mediaSeasonRatingsService.upsert(eq(profile), eq(mediaSeason), eq(rating))).thenReturn(savedRating);

        // When
        ratingService.rate(request);

        // Then
        verify(profilesService).findByIdOrThrow(eq(profileId), any());
        verify(mediaSeasonsService).findByIdOrThrow(eq(mediaSeasonId), any());
        verify(mediaSeasonRatingsService).upsert(eq(profile), eq(mediaSeason), eq(rating));
    }

    @Test
    void rate_withMinimumValidRating_shouldSucceed() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Float rating = 1.0f;

        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        RateRequestDTO request = new RateRequestDTO(profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, rating);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaItemsService.findByIdOrThrow(eq(mediaItemId), any())).thenReturn(mediaItem);

        // When/Then
        assertDoesNotThrow(() -> ratingService.rate(request));
        verify(mediaItemRatingsService).upsert(any(), any(), eq(rating));
    }

    @Test
    void rate_withMaximumValidRating_shouldSucceed() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Float rating = 10.0f;

        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        RateRequestDTO request = new RateRequestDTO(profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, rating);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaItemsService.findByIdOrThrow(eq(mediaItemId), any())).thenReturn(mediaItem);

        // When/Then
        assertDoesNotThrow(() -> ratingService.rate(request));
        verify(mediaItemRatingsService).upsert(any(), any(), eq(rating));
    }

    @Test
    void rate_withHalfStarRating_shouldSucceed() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Float rating = 7.5f;

        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        RateRequestDTO request = new RateRequestDTO(profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, rating);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaItemsService.findByIdOrThrow(eq(mediaItemId), any())).thenReturn(mediaItem);

        // When/Then
        assertDoesNotThrow(() -> ratingService.rate(request));
        verify(mediaItemRatingsService).upsert(any(), any(), eq(rating));
    }

    @Test
    void rate_withRatingBelowMinimum_shouldThrowException() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Float rating = 0.5f;

        RateRequestDTO request = new RateRequestDTO(profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, rating);

        // When/Then
        CommonExceptions.InvalidRequestExceptions exception = assertThrows(
                CommonExceptions.InvalidRequestExceptions.class,
                () -> ratingService.rate(request)
        );
        assertEquals("Rating must be between 1.0 and 10.0", exception.getMessage());
        verify(profilesService, never()).findByIdOrThrow(any(), any());
        verify(mediaItemRatingsService, never()).upsert(any(), any(), any());
    }

    @Test
    void rate_withRatingAboveMaximum_shouldThrowException() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Float rating = 10.5f;

        RateRequestDTO request = new RateRequestDTO(profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, rating);

        // When/Then
        CommonExceptions.InvalidRequestExceptions exception = assertThrows(
                CommonExceptions.InvalidRequestExceptions.class,
                () -> ratingService.rate(request)
        );
        assertEquals("Rating must be between 1.0 and 10.0", exception.getMessage());
        verify(profilesService, never()).findByIdOrThrow(any(), any());
        verify(mediaItemRatingsService, never()).upsert(any(), any(), any());
    }

    @Test
    void rate_withInvalidDecimalPlaces_shouldThrowException() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Float rating = 8.3f;

        RateRequestDTO request = new RateRequestDTO(profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, rating);

        // When/Then
        CommonExceptions.InvalidRequestExceptions exception = assertThrows(
                CommonExceptions.InvalidRequestExceptions.class,
                () -> ratingService.rate(request)
        );
        assertEquals("Rating must have only .0 or .5 decimal places", exception.getMessage());
        verify(profilesService, never()).findByIdOrThrow(any(), any());
        verify(mediaItemRatingsService, never()).upsert(any(), any(), any());
    }

    @Test
    void rate_withNullRatingMediaType_shouldThrowException() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Float rating = 8.0f;

        RateRequestDTO request = new RateRequestDTO(profileId, mediaItemId, null, rating);

        // When/Then
        CommonExceptions.InvalidRequestExceptions exception = assertThrows(
                CommonExceptions.InvalidRequestExceptions.class,
                () -> ratingService.rate(request)
        );
        assertEquals("Rating media type is required", exception.getMessage());
        verify(profilesService, never()).findByIdOrThrow(any(), any());
    }

    @Test
    void rate_withNonExistentProfile_shouldThrowException() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Float rating = 8.0f;

        RateRequestDTO request = new RateRequestDTO(profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, rating);

        when(profilesService.findByIdOrThrow(eq(profileId), any()))
                .thenThrow(new ProfilesExceptions.ProfileNotFoundExceptions(profileId));

        // When/Then
        assertThrows(
                ProfilesExceptions.ProfileNotFoundExceptions.class,
                () -> ratingService.rate(request)
        );
        verify(profilesService).findByIdOrThrow(eq(profileId), any());
        verify(mediaItemRatingsService, never()).upsert(any(), any(), any());
    }

    @Test
    void rate_withNonExistentMediaItem_shouldThrowException() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Float rating = 8.0f;

        Profile profile = new Profile("Test Profile", null);
        RateRequestDTO request = new RateRequestDTO(profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, rating);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaItemsService.findByIdOrThrow(eq(mediaItemId), any()))
                .thenThrow(new MediaItemExceptions.MediaItemNotFoundExceptions(mediaItemId));

        // When/Then
        assertThrows(
                MediaItemExceptions.MediaItemNotFoundExceptions.class,
                () -> ratingService.rate(request)
        );
        verify(mediaItemsService).findByIdOrThrow(eq(mediaItemId), any());
        verify(mediaItemRatingsService, never()).upsert(any(), any(), any());
    }

    @Test
    void rate_withNonExistentMediaSeason_shouldThrowException() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaSeasonId = UUID.randomUUID();
        Float rating = 8.0f;

        Profile profile = new Profile("Test Profile", null);
        RateRequestDTO request = new RateRequestDTO(profileId, mediaSeasonId, RatingMediaType.MEDIA_SEASON, rating);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaSeasonsService.findByIdOrThrow(eq(mediaSeasonId), any()))
                .thenThrow(new MediaSeasonExceptions.MediaSeasonNotFoundExceptions(mediaSeasonId));

        // When/Then
        assertThrows(
                MediaSeasonExceptions.MediaSeasonNotFoundExceptions.class,
                () -> ratingService.rate(request)
        );
        verify(mediaSeasonsService).findByIdOrThrow(eq(mediaSeasonId), any());
        verify(mediaSeasonRatingsService, never()).upsert(any(), any(), any());
    }

    @Test
    void deleteMediaItemRating_withExistingRating_shouldSucceed() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();

        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaItemRating rating = new MediaItemRating(profile, mediaItem, 8.0f);

        DeleteRateRequestDTO request = new DeleteRateRequestDTO(profileId, mediaItemId, RatingMediaType.MEDIA_ITEM);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaItemsService.findByIdOrThrow(eq(mediaItemId), any())).thenReturn(mediaItem);
        when(mediaItemRatingsService.findByProfileAndMediaItem(eq(profile), eq(mediaItem)))
                .thenReturn(Optional.of(rating));

        // When
        ratingService.deleteRating(request);

        // Then
        verify(profilesService).findByIdOrThrow(eq(profileId), any());
        verify(mediaItemsService).findByIdOrThrow(eq(mediaItemId), any());
        verify(mediaItemRatingsService).findByProfileAndMediaItem(eq(profile), eq(mediaItem));
        verify(mediaItemRatingsService).delete(any());
    }

    @Test
    void deleteMediaSeasonRating_withExistingRating_shouldSucceed() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaSeasonId = UUID.randomUUID();

        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of()
        );
        MediaSeason mediaSeason = new MediaSeason(mediaItem, null, 1, null, Set.of());
        MediaSeasonRating rating = new MediaSeasonRating(profile, mediaSeason, 9.0f);

        DeleteRateRequestDTO request = new DeleteRateRequestDTO(profileId, mediaSeasonId, RatingMediaType.MEDIA_SEASON);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaSeasonsService.findByIdOrThrow(eq(mediaSeasonId), any())).thenReturn(mediaSeason);
        when(mediaSeasonRatingsService.findByProfileAndMediaSeason(eq(profile), eq(mediaSeason)))
                .thenReturn(Optional.of(rating));

        // When
        ratingService.deleteRating(request);

        // Then
        verify(profilesService).findByIdOrThrow(eq(profileId), any());
        verify(mediaSeasonsService).findByIdOrThrow(eq(mediaSeasonId), any());
        verify(mediaSeasonRatingsService).findByProfileAndMediaSeason(eq(profile), eq(mediaSeason));
        verify(mediaSeasonRatingsService).delete(any());
    }

    @Test
    void deleteRating_withNonExistentRating_shouldThrowException() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();

        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );

        DeleteRateRequestDTO request = new DeleteRateRequestDTO(profileId, mediaItemId, RatingMediaType.MEDIA_ITEM);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaItemsService.findByIdOrThrow(eq(mediaItemId), any())).thenReturn(mediaItem);
        when(mediaItemRatingsService.findByProfileAndMediaItem(eq(profile), eq(mediaItem)))
                .thenReturn(Optional.empty());

        // When/Then
        CommonExceptions.ResourceNotFoundExceptions exception = assertThrows(
                CommonExceptions.ResourceNotFoundExceptions.class,
                () -> ratingService.deleteRating(request)
        );
        assertTrue(exception.getMessage().contains("Rating not found for media item ID"));
        verify(mediaItemRatingsService, never()).delete(any());
    }

    @Test
    void deleteRating_withNullRatingMediaType_shouldThrowException() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();

        DeleteRateRequestDTO request = new DeleteRateRequestDTO(profileId, mediaItemId, null);

        // When/Then
        CommonExceptions.InvalidRequestExceptions exception = assertThrows(
                CommonExceptions.InvalidRequestExceptions.class,
                () -> ratingService.deleteRating(request)
        );
        assertEquals("Rating media type is required", exception.getMessage());
        verify(profilesService, never()).findByIdOrThrow(any(), any());
    }

    @Test
    void deleteRating_withNonExistentProfile_shouldThrowException() {
        // Given
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();

        DeleteRateRequestDTO request = new DeleteRateRequestDTO(profileId, mediaItemId, RatingMediaType.MEDIA_ITEM);

        when(profilesService.findByIdOrThrow(eq(profileId), any()))
                .thenThrow(new ProfilesExceptions.ProfileNotFoundExceptions(profileId));

        // When/Then
        assertThrows(
                ProfilesExceptions.ProfileNotFoundExceptions.class,
                () -> ratingService.deleteRating(request)
        );
        verify(mediaItemRatingsService, never()).delete(any());
    }

    // --- Reviews ---

    private MediaItem newMovie() {
        return new MediaItem(
                MediaType.MOVIE, "Test Movie", null, "123", "tt123",
                2023, 120, Set.of(), Set.of(), Set.of(), Set.of()
        );
    }

    /** Stubs updateExistingReview to run the mutator against the given rating and return it. */
    private void stubReviewMutationOn(Profile profile, MediaItem mediaItem, MediaItemRating rating) {
        when(mediaItemRatingsService.updateExistingReview(eq(profile), eq(mediaItem), any()))
                .thenAnswer(inv -> {
                    Consumer<MediaItemRating> mutator = inv.getArgument(2);
                    mutator.accept(rating);
                    return rating;
                });
    }

    @Test
    void setReview_freeText_shouldSanitizeAndSave() {
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = newMovie();
        MediaItemRating rating = new MediaItemRating(profile, mediaItem, 8.0f);

        ReviewRequestDTO request = new ReviewRequestDTO(
                profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, MediaReviewType.FREE_TEXT,
                "<p>Loved it</p><script>alert(1)</script>",
                null, null, null, null, null, null);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaItemsService.findByIdOrThrow(eq(mediaItemId), any())).thenReturn(mediaItem);
        stubReviewMutationOn(profile, mediaItem, rating);

        ratingService.setReview(request);

        assertEquals(MediaReviewType.FREE_TEXT, rating.reviewType());
        assertEquals("<p>Loved it</p>", rating.reviewText());
        assertNull(rating.reviewStory());
        assertNull(rating.reviewVerdict());
        verify(mediaItemRatingsService).updateExistingReview(eq(profile), eq(mediaItem), any());
    }

    @Test
    void setReview_structured_shouldSaveProvidedCategories() {
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = newMovie();
        MediaItemRating rating = new MediaItemRating(profile, mediaItem, 8.0f);

        ReviewRequestDTO request = new ReviewRequestDTO(
                profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, MediaReviewType.STRUCTURED,
                null,
                "<strong>Tight plot</strong>", null, null, null, null,
                "<em>Great</em>");

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaItemsService.findByIdOrThrow(eq(mediaItemId), any())).thenReturn(mediaItem);
        stubReviewMutationOn(profile, mediaItem, rating);

        ratingService.setReview(request);

        assertEquals(MediaReviewType.STRUCTURED, rating.reviewType());
        assertEquals("<strong>Tight plot</strong>", rating.reviewStory());
        assertEquals("<em>Great</em>", rating.reviewVerdict());
        assertNull(rating.reviewText());
        assertNull(rating.reviewPerformances());
    }

    @Test
    void setReview_freeTextWithCategory_shouldThrow() {
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = newMovie();

        ReviewRequestDTO request = new ReviewRequestDTO(
                profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, MediaReviewType.FREE_TEXT,
                "free", "structured story", null, null, null, null, null);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);

        assertThrows(CommonExceptions.InvalidRequestExceptions.class, () -> ratingService.setReview(request));
        verify(mediaItemRatingsService, never()).updateExistingReview(any(), any(), any());
    }

    @Test
    void setReview_freeTextBlank_shouldThrow() {
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = newMovie();

        ReviewRequestDTO request = new ReviewRequestDTO(
                profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, MediaReviewType.FREE_TEXT,
                "   ", null, null, null, null, null, null);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);

        assertThrows(CommonExceptions.InvalidRequestExceptions.class, () -> ratingService.setReview(request));
        verify(mediaItemRatingsService, never()).updateExistingReview(any(), any(), any());
    }

    @Test
    void setReview_structuredWithFreeText_shouldThrow() {
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = newMovie();

        ReviewRequestDTO request = new ReviewRequestDTO(
                profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, MediaReviewType.STRUCTURED,
                "free text here", "story", null, null, null, null, null);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);

        assertThrows(CommonExceptions.InvalidRequestExceptions.class, () -> ratingService.setReview(request));
        verify(mediaItemRatingsService, never()).updateExistingReview(any(), any(), any());
    }

    @Test
    void setReview_structuredNoCategories_shouldThrow() {
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = newMovie();

        ReviewRequestDTO request = new ReviewRequestDTO(
                profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, MediaReviewType.STRUCTURED,
                null, null, null, null, null, null, null);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);

        assertThrows(CommonExceptions.InvalidRequestExceptions.class, () -> ratingService.setReview(request));
        verify(mediaItemRatingsService, never()).updateExistingReview(any(), any(), any());
    }

    @Test
    void setReview_structuredCategoryOnlyXss_shouldThrowAfterSanitize() {
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = newMovie();

        // Sanitizes to nothing -> treated as no category provided.
        ReviewRequestDTO request = new ReviewRequestDTO(
                profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, MediaReviewType.STRUCTURED,
                null, "<script>alert(1)</script>", null, null, null, null, null);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);

        assertThrows(CommonExceptions.InvalidRequestExceptions.class, () -> ratingService.setReview(request));
        verify(mediaItemRatingsService, never()).updateExistingReview(any(), any(), any());
    }

    @Test
    void setReview_nullType_shouldThrowBeforeLoading() {
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();

        ReviewRequestDTO request = new ReviewRequestDTO(
                profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, null,
                "free", null, null, null, null, null, null);

        CommonExceptions.InvalidRequestExceptions ex = assertThrows(
                CommonExceptions.InvalidRequestExceptions.class,
                () -> ratingService.setReview(request));
        assertEquals("Review type is required", ex.getMessage());
        verify(profilesService, never()).findByIdOrThrow(any(), any());
    }

    @Test
    void setReview_unratedItem_shouldPropagateNotFound() {
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = newMovie();

        ReviewRequestDTO request = new ReviewRequestDTO(
                profileId, mediaItemId, RatingMediaType.MEDIA_ITEM, MediaReviewType.FREE_TEXT,
                "free", null, null, null, null, null, null);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaItemsService.findByIdOrThrow(eq(mediaItemId), any())).thenReturn(mediaItem);
        when(mediaItemRatingsService.updateExistingReview(eq(profile), eq(mediaItem), any()))
                .thenThrow(new CommonExceptions.ResourceNotFoundExceptions("not rated"));

        assertThrows(CommonExceptions.ResourceNotFoundExceptions.class, () -> ratingService.setReview(request));
    }

    @Test
    void deleteReview_shouldClearAllReviewFields() {
        UUID profileId = UUID.randomUUID();
        UUID mediaItemId = UUID.randomUUID();
        Profile profile = new Profile("Test Profile", null);
        MediaItem mediaItem = newMovie();
        MediaItemRating rating = new MediaItemRating(profile, mediaItem, 8.0f);
        rating.setReviewType(MediaReviewType.FREE_TEXT);
        rating.setReviewText("<p>old</p>");
        rating.setReviewStory("<p>old story</p>");

        DeleteReviewRequestDTO request = new DeleteReviewRequestDTO(profileId, mediaItemId, RatingMediaType.MEDIA_ITEM);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaItemsService.findByIdOrThrow(eq(mediaItemId), any())).thenReturn(mediaItem);
        stubReviewMutationOn(profile, mediaItem, rating);

        ratingService.deleteReview(request);

        assertNull(rating.reviewType());
        assertNull(rating.reviewText());
        assertNull(rating.reviewStory());
    }

    @Test
    void setReview_season_freeText_shouldSanitizeAndSave() {
        UUID profileId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        Profile profile = new Profile("Test Profile", null);
        MediaItem series = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of());
        MediaSeason season = new MediaSeason(series, null, 1, null, Set.of());
        MediaSeasonRating rating = new MediaSeasonRating(profile, season, 8.0f);

        ReviewRequestDTO request = new ReviewRequestDTO(
                profileId, seasonId, RatingMediaType.MEDIA_SEASON, MediaReviewType.FREE_TEXT,
                "<p>Strong season</p><script>x()</script>",
                null, null, null, null, null, null);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaSeasonsService.findByIdOrThrow(eq(seasonId), any())).thenReturn(season);
        when(mediaSeasonRatingsService.updateExistingReview(eq(profile), eq(season), any()))
                .thenAnswer(inv -> {
                    Consumer<MediaSeasonRating> mutator = inv.getArgument(2);
                    mutator.accept(rating);
                    return rating;
                });

        ratingService.setReview(request);

        assertEquals(MediaReviewType.FREE_TEXT, rating.reviewType());
        assertEquals("<p>Strong season</p>", rating.reviewText());
        verify(mediaSeasonRatingsService).updateExistingReview(eq(profile), eq(season), any());
        verify(mediaItemRatingsService, never()).updateExistingReview(any(), any(), any());
    }

    @Test
    void setReview_season_structured_shouldSave() {
        UUID profileId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        Profile profile = new Profile("Test Profile", null);
        MediaItem series = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of());
        MediaSeason season = new MediaSeason(series, null, 2, null, Set.of());
        MediaSeasonRating rating = new MediaSeasonRating(profile, season, 7.0f);

        ReviewRequestDTO request = new ReviewRequestDTO(
                profileId, seasonId, RatingMediaType.MEDIA_SEASON, MediaReviewType.STRUCTURED,
                null, "<em>Pacing dipped</em>", null, null, null, null, null);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaSeasonsService.findByIdOrThrow(eq(seasonId), any())).thenReturn(season);
        when(mediaSeasonRatingsService.updateExistingReview(eq(profile), eq(season), any()))
                .thenAnswer(inv -> {
                    Consumer<MediaSeasonRating> mutator = inv.getArgument(2);
                    mutator.accept(rating);
                    return rating;
                });

        ratingService.setReview(request);

        assertEquals(MediaReviewType.STRUCTURED, rating.reviewType());
        assertEquals("<em>Pacing dipped</em>", rating.reviewStory());
        assertNull(rating.reviewText());
    }

    @Test
    void setReview_season_unrated_shouldPropagateNotFound() {
        UUID profileId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        Profile profile = new Profile("Test Profile", null);
        MediaItem series = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of());
        MediaSeason season = new MediaSeason(series, null, 1, null, Set.of());

        ReviewRequestDTO request = new ReviewRequestDTO(
                profileId, seasonId, RatingMediaType.MEDIA_SEASON, MediaReviewType.FREE_TEXT,
                "free", null, null, null, null, null, null);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaSeasonsService.findByIdOrThrow(eq(seasonId), any())).thenReturn(season);
        when(mediaSeasonRatingsService.updateExistingReview(eq(profile), eq(season), any()))
                .thenThrow(new CommonExceptions.ResourceNotFoundExceptions("not rated"));

        assertThrows(CommonExceptions.ResourceNotFoundExceptions.class, () -> ratingService.setReview(request));
    }

    @Test
    void deleteReview_season_shouldClearAllReviewFields() {
        UUID profileId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        Profile profile = new Profile("Test Profile", null);
        MediaItem series = new MediaItem(
                MediaType.SERIES, "Test Series", null, "123", "tt123",
                2023, 60, Set.of(), Set.of(), Set.of(), Set.of());
        MediaSeason season = new MediaSeason(series, null, 1, null, Set.of());
        MediaSeasonRating rating = new MediaSeasonRating(profile, season, 8.0f);
        rating.setReviewType(MediaReviewType.STRUCTURED);
        rating.setReviewStory("<p>old</p>");

        DeleteReviewRequestDTO request = new DeleteReviewRequestDTO(profileId, seasonId, RatingMediaType.MEDIA_SEASON);

        when(profilesService.findByIdOrThrow(eq(profileId), any())).thenReturn(profile);
        when(mediaSeasonsService.findByIdOrThrow(eq(seasonId), any())).thenReturn(season);
        when(mediaSeasonRatingsService.updateExistingReview(eq(profile), eq(season), any()))
                .thenAnswer(inv -> {
                    Consumer<MediaSeasonRating> mutator = inv.getArgument(2);
                    mutator.accept(rating);
                    return rating;
                });

        ratingService.deleteReview(request);

        assertNull(rating.reviewType());
        assertNull(rating.reviewStory());
    }

    @Test
    void setReview_nullRatingMediaType_shouldThrow() {
        UUID profileId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();

        ReviewRequestDTO request = new ReviewRequestDTO(
                profileId, entityId, null, MediaReviewType.FREE_TEXT,
                "free", null, null, null, null, null, null);

        CommonExceptions.InvalidRequestExceptions ex = assertThrows(
                CommonExceptions.InvalidRequestExceptions.class,
                () -> ratingService.setReview(request));
        assertEquals("Rating media type is required", ex.getMessage());
        verify(profilesService, never()).findByIdOrThrow(any(), any());
    }

    @Test
    void exportProfileMediaRatingsCsv_shouldIncludeRequestedColumnsAndTopCredits() throws IOException {
        // Given
        Profile profile = new Profile("Test Profile", null);

        MediaItem mediaItem = new MediaItem(
                MediaType.MOVIE, "Inception", null, "27205", "tt1375666",
                2010, 148, Set.of(), Set.of(), Set.of(), Set.of()
        );
        UUID mediaItemId = UUID.randomUUID();
        ReflectionTestUtils.setField(mediaItem, "id", mediaItemId);

        MediaItemRating rating = new MediaItemRating(profile, mediaItem, 9.5f);

        Person actor1 = new Person("Leonardo DiCaprio", "1", null);
        Person actor2 = new Person("Joseph Gordon-Levitt", "2", null);
        Person director = new Person("Christopher Nolan", "3", null);
        Person producer = new Person("Emma Thomas", "4", null);

        MediaItemCast cast1 = new MediaItemCast(mediaItem, actor1, "Cobb", 0);
        MediaItemCast cast2 = new MediaItemCast(mediaItem, actor2, "Arthur", 1);
        MediaItemCrew directorCrew = new MediaItemCrew(mediaItem, director, "Director", "Directing");
        MediaItemCrew producerCrew = new MediaItemCrew(mediaItem, producer, "Producer", "Production");

        when(mediaItemRatingsService.findAllByProfile(profile)).thenReturn(java.util.List.of(rating));
        when(mediaItemCastRepository.findByMediaItemIdIn(java.util.List.of(mediaItemId))).thenReturn(java.util.List.of(cast2, cast1));
        when(mediaItemCrewRepository.findByMediaItemIdIn(java.util.List.of(mediaItemId))).thenReturn(java.util.List.of(directorCrew, producerCrew));

        // When
        byte[] csvBytes = ratingService.exportProfileMediaRatingsCsv(profile);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        // Then
        CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(new StringReader(csv));
        var records = parser.getRecords();
        assertEquals(1, records.size());

        var row = records.getFirst();
        assertEquals("Inception", row.get("title"));
        assertEquals("MOVIE", row.get("media type"));
        assertEquals("9.5", row.get("my rating"));
        assertEquals("tt1375666", row.get("imdb id"));
        assertEquals("27205", row.get("tmdb id"));
        assertEquals("Leonardo DiCaprio", row.get("top actors 1"));
        assertEquals("Joseph Gordon-Levitt", row.get("top actors 2"));
        assertEquals("Christopher Nolan", row.get("director 1"));
        assertEquals("Emma Thomas", row.get("producer 1"));
        verify(mediaItemCastRepository).findByMediaItemIdIn(java.util.List.of(mediaItemId));
        verify(mediaItemCrewRepository).findByMediaItemIdIn(java.util.List.of(mediaItemId));
        verify(mediaItemCastRepository, never()).findByMediaItemId(any());
        verify(mediaItemCrewRepository, never()).findByMediaItemId(any());
    }

    @Test
    void exportProfileMediaRatingsCsv_withMissingCredits_shouldKeepColumnsEmpty() throws IOException {
        // Given
        Profile profile = new Profile("Test Profile", null);

        MediaItem mediaItem = new MediaItem(
                MediaType.SERIES, "Severance", null, "95396", "tt11280740",
                2022, 50, Set.of(), Set.of(), Set.of(), Set.of()
        );
        UUID mediaItemId = UUID.randomUUID();
        ReflectionTestUtils.setField(mediaItem, "id", mediaItemId);

        MediaItemRating rating = new MediaItemRating(profile, mediaItem, 8.0f);

        when(mediaItemRatingsService.findAllByProfile(profile)).thenReturn(java.util.List.of(rating));
        when(mediaItemCastRepository.findByMediaItemIdIn(java.util.List.of(mediaItemId))).thenReturn(java.util.List.of());
        when(mediaItemCrewRepository.findByMediaItemIdIn(java.util.List.of(mediaItemId))).thenReturn(java.util.List.of());

        // When
        byte[] csvBytes = ratingService.exportProfileMediaRatingsCsv(profile);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        // Then
        CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build().parse(new StringReader(csv));
        var records = parser.getRecords();
        assertEquals(1, records.size());

        var row = records.getFirst();
        assertEquals("", row.get("top actors 1"));
        assertEquals("", row.get("top actors 5"));
        assertEquals("", row.get("director 1"));
        assertEquals("", row.get("director 3"));
        assertEquals("", row.get("producer 1"));
        assertEquals("", row.get("producer 3"));
        verify(mediaItemCastRepository).findByMediaItemIdIn(java.util.List.of(mediaItemId));
        verify(mediaItemCrewRepository).findByMediaItemIdIn(java.util.List.of(mediaItemId));
        verify(mediaItemCastRepository, never()).findByMediaItemId(any());
        verify(mediaItemCrewRepository, never()).findByMediaItemId(any());
    }
}
