package com.rattatarr.rattatarr.clients.jellyfin.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rattatarr.rattatarr.models.JellyfinMediaType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JellyfinClientItemManualPlaylistsFolderResponseDTO(
        @JsonProperty("Id") String id,
        @JsonProperty("Name") String name,
        @JsonProperty("Type") JellyfinMediaType mediaType
) implements JellyfinClientItemResponseDTO {
}
