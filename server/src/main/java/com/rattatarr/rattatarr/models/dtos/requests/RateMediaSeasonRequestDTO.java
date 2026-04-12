package com.rattatarr.rattatarr.models.dtos.requests;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.UUID;

public record RateMediaSeasonRequestDTO(
        @NotNull(message = "Profile ID is required")
        UUID profileId,

        @NotNull(message = "Media season ID is required")
        UUID mediaSeasonId,

        @NotNull(message = "Rating is required")
        @DecimalMin(value = "1.0", message = "Rating must be at least 0.0")
        @DecimalMax(value = "10.0", message = "Rating cannot exceed 10.0")
        @Digits(integer = 2, fraction = 1, message = "Rating must have at most 1 decimal place")
        Float rating
) implements Serializable {
}
