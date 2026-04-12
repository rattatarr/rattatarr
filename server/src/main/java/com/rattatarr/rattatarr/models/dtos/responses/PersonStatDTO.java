package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PersonStatDTO(
        UUID personId,
        String name,
        String profilePathUrl,
        Double averageRating,
        Long itemCount
) implements Serializable {
}
