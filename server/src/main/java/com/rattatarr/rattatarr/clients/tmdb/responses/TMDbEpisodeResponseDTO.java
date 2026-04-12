package com.rattatarr.rattatarr.clients.tmdb.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TMDbEpisodeResponseDTO(
        Integer id,
        String name,
        String overview,
        @JsonProperty("episode_number") Integer episodeNumber,
        @JsonProperty("season_number") Integer seasonNumber,
        @JsonProperty("air_date") String airDate,
        Integer runtime,
        @JsonProperty("still_path") String stillPath,
        @JsonProperty("vote_average") Double voteAverage,
        @JsonProperty("vote_count") Integer voteCount
) implements Serializable {
}
