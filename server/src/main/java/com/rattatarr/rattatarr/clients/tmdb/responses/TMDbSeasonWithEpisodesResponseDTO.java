package com.rattatarr.rattatarr.clients.tmdb.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TMDbSeasonWithEpisodesResponseDTO(
        @JsonProperty("_id") String id,
        String name,
        String overview,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("air_date") String airDate,
        @JsonProperty("season_number") Integer seasonNumber,
        @JsonProperty("vote_average") Double voteAverage,
        List<TMDbEpisodeResponseDTO> episodes
) implements Serializable {
}
