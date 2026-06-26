package com.rattatarr.rattatarr.models.dtos.requests;

import com.rattatarr.rattatarr.models.MediaReviewType;
import com.rattatarr.rattatarr.models.RatingMediaType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.UUID;

public record ReviewRequestDTO(
        @NotNull(message = "Profile ID is required")
        UUID profileId,

        @NotNull(message = "Media item ID is required")
        UUID entityId,

        @NotNull(message = "Rating media type is required")
        RatingMediaType ratingMediaType,

        @NotNull(message = "Review type is required")
        MediaReviewType reviewType,

        @Size(max = 20000, message = "Review text is too long")
        String reviewText,

        @Size(max = 20000, message = "Story review is too long")
        String reviewStory,

        @Size(max = 20000, message = "Performances review is too long")
        String reviewPerformances,

        @Size(max = 20000, message = "Direction review is too long")
        String reviewDirection,

        @Size(max = 20000, message = "Visuals review is too long")
        String reviewVisuals,

        @Size(max = 20000, message = "Sound review is too long")
        String reviewSound,

        @Size(max = 20000, message = "Verdict review is too long")
        String reviewVerdict
) implements Serializable {
}
