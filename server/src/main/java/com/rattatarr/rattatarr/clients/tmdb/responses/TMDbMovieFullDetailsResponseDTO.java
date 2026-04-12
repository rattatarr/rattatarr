package com.rattatarr.rattatarr.clients.tmdb.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TMDbMovieFullDetailsResponseDTO(
        Integer id,
        String overview,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("backdrop_path") String backdropPath,
        TMDbCreditsResponseDTO credits,
        String homepage,
        String title,
        String original_title,
        String original_language,
        String status,
        @JsonProperty("imdb_id") String IMDbId,
        List<TMDbGenreResponseDTO> genres,
        Integer runtime,
        @JsonProperty("release_date") String releaseDate
) implements Serializable {
}
