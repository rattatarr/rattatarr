package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MediaTypeBreakdownDTO(
        String mediaType,
        Long count,
        Long totalCount,
        Double percentage,
        Double averageRating
) implements Serializable {
}
