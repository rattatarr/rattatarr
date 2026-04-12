package com.rattatarr.rattatarr.models.dtos.requests;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

public record MoviesFiltersDTO(
        UUID id,
        @NotBlank(message = "Title cannot be blank")
        String title,
        Integer releasedAfter,
        Integer releasedBefore,
        Set<String> genres,
        String posterSize,
        String backdropSize,
        String profileSize,
        UUID profileId,
        Float ratingMin,
        Float ratingMax,
        UUID personId,
        Boolean unrated
) implements Serializable {
}
