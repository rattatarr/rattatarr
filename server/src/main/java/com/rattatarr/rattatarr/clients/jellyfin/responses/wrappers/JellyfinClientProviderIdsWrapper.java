package com.rattatarr.rattatarr.clients.jellyfin.responses.wrappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JellyfinClientProviderIdsWrapper(
        @JsonProperty("Imdb") String IMDbId,
        @JsonProperty("Tmdb") String TMDbId
) implements Serializable {
}
