package com.rattatarr.rattatarr.clients.tmdb.responses;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TMDbFindItemResponseDTO(
        Integer id,
        @JsonAlias({"title", "name"}) String title,
        @JsonAlias({"description", "overview"}) String description,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("backdrop_path") String backdropPath,
        @JsonAlias({"release_date", "first_air_date"}) String releaseDate
) implements Serializable {
}
