package com.rattatarr.rattatarr.clients.jellyfin.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rattatarr.rattatarr.clients.jellyfin.responses.wrappers.JellyfinClientProviderIdsWrapper;
import com.rattatarr.rattatarr.models.JellyfinMediaType;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JellyfinClientItemEpisodeResponseDTO(
        @JsonProperty("Name") String name,
        @JsonProperty("Id") String id,
        @JsonProperty("Type") JellyfinMediaType mediaType,
        @JsonProperty("Genres") List<String> genres,
        @JsonProperty("ProviderIds") JellyfinClientProviderIdsWrapper providers,
        @JsonProperty("ProductionYear") Integer productionYear,
        @JsonProperty("IndexNumber") Integer episodeNumber,
        @JsonProperty("RunTimeTicks") Long runTimeTicks
) implements JellyfinClientItemResponseDTO, Serializable, JellyfinClientItemCommonsResponseDTO {
}
