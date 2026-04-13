package com.rattatarr.rattatarr.clients.jellyfin.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JellyfinClientPlaybackItemResponseDTO(
        @JsonProperty("Id") String id,
        @JsonProperty("Type") String type,
        @JsonProperty("Name") String name,
        @JsonProperty("UserData") JellyfinClientPlaybackItemUserDataResponseDTO userData
) implements Serializable {
}
