package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RatingConsistencyDTO(
        Double standardDeviation,
        Double variance,
        String consistencyLevel
) implements Serializable {
}
