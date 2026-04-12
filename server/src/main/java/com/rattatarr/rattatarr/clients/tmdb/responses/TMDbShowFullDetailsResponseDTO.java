package com.rattatarr.rattatarr.clients.tmdb.responses;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TMDbShowFullDetailsResponseDTO(
        Integer id,
        String overview,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("backdrop_path") String backdropPath,
        List<TMDbSeasonResponseDTO> seasons,
        TMDbCreditsResponseDTO credits,
        String homepage,
        String name,
        String original_name,
        String original_language,
        String status,
        @JsonProperty("imdb_id") String IMDbId,
        List<TMDbGenreResponseDTO> genres,
        @JsonProperty("number_of_episodes") Integer numberOfEpisodes,
        @JsonProperty("number_of_seasons") Integer numberOfSeasons,
        @JsonProperty("first_air_date") String firstAirDate,
        // Allow dynamic season details with episodes to be captured: season/1, season/2, ...
        @JsonAnySetter Map<String, Object> seasonDetails,
        @JsonProperty("episode_run_time") List<Integer> episodeRunTimeMinutes
) implements Serializable {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public TMDbShowFullDetailsResponseDTO {
        if (seasonDetails == null) {
            seasonDetails = new HashMap<>();
        }
    }

    /**
     * Get season details with episodes for a specific season number.
     * The season details are returned from TMDb when using append_to_response=season/1,season/2,...
     *
     * @param seasonNumber the season number to get details for
     * @return the season with episodes, or null if not available
     */
    public TMDbSeasonWithEpisodesResponseDTO getSeasonWithEpisodes(int seasonNumber) {
        String key = "season/" + seasonNumber;
        Object value = seasonDetails.get(key);
        if (value == null) {
            return null;
        }
        return MAPPER.convertValue(value, TMDbSeasonWithEpisodesResponseDTO.class);
    }

    /**
     * Get all season details with episodes that were fetched.
     *
     * @return map of season number to season details with episodes
     */
    public Map<Integer, TMDbSeasonWithEpisodesResponseDTO> getAllSeasonDetails() {
        Map<Integer, TMDbSeasonWithEpisodesResponseDTO> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : seasonDetails.entrySet()) {
            if (entry.getKey().startsWith("season/")) {
                try {
                    int seasonNum = Integer.parseInt(entry.getKey().substring(7));
                    TMDbSeasonWithEpisodesResponseDTO seasonData =
                            MAPPER.convertValue(entry.getValue(), TMDbSeasonWithEpisodesResponseDTO.class);
                    result.put(seasonNum, seasonData);
                } catch (NumberFormatException ignored) {
                    // Skip invalid season keys
                }
            }
        }
        return result;
    }
}
