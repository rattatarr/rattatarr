package com.rattatarr.rattatarr.clients.tmdb.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TMDbSearchResponseDTO(
        Integer page,
        List<TMDbFindItemResponseDTO> results,
        @JsonProperty("total_pages") Integer totalPages,
        @JsonProperty("total_results") Integer totalResults
) implements Serializable {
}
