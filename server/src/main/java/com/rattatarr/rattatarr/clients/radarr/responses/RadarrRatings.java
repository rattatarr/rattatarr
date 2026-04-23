package com.rattatarr.rattatarr.clients.radarr.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RadarrRatings(
        @Nullable RadarrRatingEntry imdb,
        @Nullable RadarrRatingEntry rottenTomatoes
) {
}
