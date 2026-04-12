package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OverallStatsDTO(
        Long totalRatings,
        Long totalItems,
        Double averageRating,
        Float minRating,
        Float maxRating
) implements Serializable {
}
