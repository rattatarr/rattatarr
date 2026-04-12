package com.rattatarr.rattatarr.clients.jellyfin.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rattatarr.rattatarr.clients.jellyfin.responses.wrappers.JellyfinClientProviderIdsWrapper;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public sealed interface JellyfinClientItemCommonsResponseDTO
        permits JellyfinClientItemEpisodeResponseDTO,
        JellyfinClientItemMovieResponseDTO,
        JellyfinClientItemSeasonResponseDTO,
        JellyfinClientItemSeriesResponseDTO {

    @JsonProperty("Genres")
    List<String> genres();

    @JsonProperty("ProviderIds")
    JellyfinClientProviderIdsWrapper providers();

    @JsonProperty("ProductionYear")
    Integer productionYear();
}
