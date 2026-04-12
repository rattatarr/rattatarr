package com.rattatarr.rattatarr.clients.tmdb.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TMDbSeasonResponseDTO(
        @JsonProperty("episode_count") Integer episodeCount,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("air_date") String airDate,
        @JsonProperty("season_number") Integer seasonNumber,
        String name
) implements Serializable {
}
