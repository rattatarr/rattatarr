package com.rattatarr.rattatarr.clients.tmdb.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TMDbFindResponseDTO(
        @JsonProperty("movie_results") List<TMDbFindItemResponseDTO> movieResults,
        @JsonProperty("tv_results") List<TMDbFindItemResponseDTO> tvResults
) implements Serializable {
}
