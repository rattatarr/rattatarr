package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DecadeStatDTO(
        Integer decade,
        Long count,
        Double averageRating
) implements Serializable {
}
