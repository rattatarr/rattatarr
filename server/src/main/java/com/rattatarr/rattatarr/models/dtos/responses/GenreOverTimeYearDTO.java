package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GenreOverTimeYearDTO(
        int year,
        List<GenreYearStatDTO> genres
) implements Serializable {
}
