package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.MediaReviewType;
import com.rattatarr.rattatarr.models.entities.ReviewableRating;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ReviewResponseDTO(
        MediaReviewType reviewType,
        String reviewText,
        String reviewStory,
        String reviewPerformances,
        String reviewDirection,
        String reviewVisuals,
        String reviewSound,
        String reviewVerdict
) implements Serializable {
    /**
     * Builds a review DTO from the rating, or null if the rating carries no review.
     */
    public static @Nullable ReviewResponseDTO fromEntity(@Nullable ReviewableRating rating) {
        if (rating == null || rating.reviewType() == null) {
            return null;
        }
        return new ReviewResponseDTO(
                rating.reviewType(),
                rating.reviewText(),
                rating.reviewStory(),
                rating.reviewPerformances(),
                rating.reviewDirection(),
                rating.reviewVisuals(),
                rating.reviewSound(),
                rating.reviewVerdict()
        );
    }
}
