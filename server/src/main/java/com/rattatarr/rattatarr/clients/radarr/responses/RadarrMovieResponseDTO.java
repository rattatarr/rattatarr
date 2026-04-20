package com.rattatarr.rattatarr.clients.radarr.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RadarrMovieResponseDTO(
        int id,
        String title,
        int year,
        @Nullable Integer tmdbId,
        @Nullable String imdbId,
        boolean monitored,
        boolean hasFile,
        @Nullable Ratings ratings
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ratings(
            @Nullable RatingEntry imdb,
            @Nullable RatingEntry rottenTomatoes
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RatingEntry(
            int votes,
            double value,
            String type
    ) {
    }
}
