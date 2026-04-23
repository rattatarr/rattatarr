package com.rattatarr.rattatarr.clients.radarr.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RadarrRatingEntry(
        int votes,
        double value,
        String type
) {
}
