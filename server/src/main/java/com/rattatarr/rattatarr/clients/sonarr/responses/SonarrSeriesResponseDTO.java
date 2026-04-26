package com.rattatarr.rattatarr.clients.sonarr.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SonarrSeriesResponseDTO(
        int id,
        String title,
        int year,
        @Nullable Integer tvdbId,
        @Nullable Integer tmdbId
) {
}
