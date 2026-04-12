package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RatingDistributionDTO(
        String range,
        Long count,
        Double percentage
) implements Serializable {
}
