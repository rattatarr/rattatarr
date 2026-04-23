package com.rattatarr.rattatarr.clients.radarr.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RadarrMovieLookupResponseDTO(
        String title,
        int year,
        @Nullable Integer tmdbId,
        @Nullable String imdbId,
        boolean monitored,
        @Nullable RadarrRatings ratings
) {
}
