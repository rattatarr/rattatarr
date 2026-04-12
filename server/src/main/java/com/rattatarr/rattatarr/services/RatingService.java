package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.exceptions.MediaItemExceptions;
import com.rattatarr.rattatarr.exceptions.MediaSeasonExceptions;
import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.dtos.requests.DeleteRateRequestDTO;
import com.rattatarr.rattatarr.models.dtos.requests.RateRequestDTO;
import com.rattatarr.rattatarr.models.entities.*;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@NullMarked
public class RatingService {
    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);

    private final ProfilesService profilesService;
    private final MediaItemRatingsService mediaItemRatingsService;
    private final MediaSeasonRatingsService mediaSeasonRatingsService;
    private final MediaItemsService mediaItemsService;
    private final MediaSeasonsService mediaSeasonsService;

    public RatingService(
            ProfilesService profilesService,
            MediaItemRatingsService mediaItemRatingsService,
            MediaSeasonRatingsService mediaSeasonRatingsService,
            MediaItemsService mediaItemsService,
            MediaSeasonsService mediaSeasonsService
    ) {
        this.profilesService = profilesService;
        this.mediaItemRatingsService = mediaItemRatingsService;
        this.mediaSeasonRatingsService = mediaSeasonRatingsService;
        this.mediaItemsService = mediaItemsService;
        this.mediaSeasonsService = mediaSeasonsService;
    }

    private void validateRating(
            Float rating
    ) {
        if (rating < 1.0f || rating > 10.0f) {
            throw new CommonExceptions.InvalidRequestExceptions("Rating must be between 1.0 and 10.0");
        }
        float decimalPart = rating % 1;
        if (decimalPart != 0.0f && decimalPart != 0.5f) {
            throw new CommonExceptions.InvalidRequestExceptions("Rating must have only .0 or .5 decimal places");
        }
    }

    public void rate(RateRequestDTO request) {
        logger.debug("Rating request received: {}", request);

        if (request.ratingMediaType() == null) {
            throw new CommonExceptions.InvalidRequestExceptions("Rating media type is required");
        }

        Float rating = request.rating();

        validateRating(rating);

        Profile profile = profilesService.findByIdOrThrow(
                request.profileId(),
                ProfilesExceptions.ProfileNotFoundExceptions::new
        );

        switch (request.ratingMediaType()) {
            case MEDIA_ITEM -> {
                MediaItem mediaItem = mediaItemsService.findByIdOrThrow(
                        request.entityId(),
                        MediaItemExceptions.MediaItemNotFoundExceptions::new
                );

                mediaItemRatingsService.upsert(profile, mediaItem, rating);
            }
            case MEDIA_SEASON -> {
                MediaSeason mediaSeason = mediaSeasonsService.findByIdOrThrow(
                        request.entityId(),
                        MediaSeasonExceptions.MediaSeasonNotFoundExceptions::new
                );

                mediaSeasonRatingsService.upsert(profile, mediaSeason, rating);
            }
            default ->
                    throw new CommonExceptions.InvalidRequestExceptions("Unsupported rating media type: " + request.ratingMediaType());
        }
    }

    public void deleteRating(
            DeleteRateRequestDTO request
    ) {
        logger.debug("Delete rating request received: {}", request);

        if (request.ratingMediaType() == null) {
            throw new CommonExceptions.InvalidRequestExceptions("Rating media type is required");
        }

        Profile profile = profilesService.findByIdOrThrow(
                request.profileId(),
                ProfilesExceptions.ProfileNotFoundExceptions::new
        );

        switch (request.ratingMediaType()) {
            case MEDIA_ITEM -> {
                MediaItem mediaItem = mediaItemsService.findByIdOrThrow(
                        request.entityId(),
                        MediaItemExceptions.MediaItemNotFoundExceptions::new
                );

                MediaItemRating rating = mediaItemRatingsService.findByProfileAndMediaItem(profile, mediaItem)
                        .orElseThrow(() -> new CommonExceptions.ResourceNotFoundExceptions("Rating not found for media item ID: " + request.entityId()));

                mediaItemRatingsService.delete(rating.id());
            }
            case MEDIA_SEASON -> {
                MediaSeason mediaSeason = mediaSeasonsService.findByIdOrThrow(
                        request.entityId(),
                        MediaSeasonExceptions.MediaSeasonNotFoundExceptions::new
                );

                MediaSeasonRating rating = mediaSeasonRatingsService.findByProfileAndMediaSeason(profile, mediaSeason)
                        .orElseThrow(() -> new CommonExceptions.ResourceNotFoundExceptions("Rating not found for media season ID: " + request.entityId()));

                mediaSeasonRatingsService.delete(rating.id());
            }
            default ->
                    throw new CommonExceptions.InvalidRequestExceptions("Unsupported rating media type: " + request.ratingMediaType());
        }
    }
}
