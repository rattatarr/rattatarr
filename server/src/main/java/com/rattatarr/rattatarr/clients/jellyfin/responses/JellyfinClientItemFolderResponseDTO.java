package com.rattatarr.rattatarr.clients.jellyfin.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rattatarr.rattatarr.models.JellyfinMediaType;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JellyfinClientItemFolderResponseDTO(
        @JsonProperty("Name") String name,
        @JsonProperty("Id") String id,
        @JsonProperty("Type") JellyfinMediaType mediaType
) implements JellyfinClientItemResponseDTO, Serializable {
}
