package com.rattatarr.rattatarr.models.dtos.requests;

import com.rattatarr.rattatarr.models.RatingMediaType;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.UUID;

public record DeleteReviewRequestDTO(
        @NotNull(message = "Profile ID is required")
        UUID profileId,

        @NotNull(message = "Media item ID is required")
        UUID entityId,

        @NotNull(message = "Rating media type is required")
        RatingMediaType ratingMediaType
) implements Serializable {
}
