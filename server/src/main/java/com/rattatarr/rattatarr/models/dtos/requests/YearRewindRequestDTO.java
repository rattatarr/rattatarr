package com.rattatarr.rattatarr.models.dtos.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record YearRewindRequestDTO(
        @NotNull(message = "Profile ID is required")
        UUID profileId,

        @NotNull(message = "Year is required")
        @Min(value = 1900, message = "Year must be at least 1900")
        @Max(value = 2100, message = "Year must be at most 2100")
        Integer year,

        @DecimalMin(value = "0.0", message = "Rating threshold must be at least 0.0")
        Float ratingThreshold,

        @Min(value = 1, message = "Minimum count must be at least 1")
        Integer minCount,

        @Min(value = 1, message = "Genres limit must be at least 1")
        Integer genresLimit,

        @Min(value = 1, message = "Actors limit must be at least 1")
        Integer actorsLimit,

        @Min(value = 1, message = "Directors limit must be at least 1")
        Integer directorsLimit,

        @Min(value = 1, message = "Producers limit must be at least 1")
        Integer producersLimit,

        String profileImageSize
) {
    public YearRewindRequestDTO {
        if (ratingThreshold == null) ratingThreshold = 7.0f;
        if (minCount == null) minCount = 1;
        if (genresLimit == null) genresLimit = 10;
        if (actorsLimit == null) actorsLimit = 10;
        if (directorsLimit == null) directorsLimit = 10;
        if (producersLimit == null) producersLimit = 10;
        if (profileImageSize == null) profileImageSize = "w185";
    }
}
