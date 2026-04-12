package com.rattatarr.rattatarr.clients.jellyfin.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JellyfinClientUserResponseDTO(
        @JsonProperty("Name") String name,
        @JsonProperty("Id") String id,
        @JsonProperty("LastLoginDate") String lastLoginDate,
        @JsonProperty("LastActivityDate") String lastActivityDate
) implements Serializable {
}
