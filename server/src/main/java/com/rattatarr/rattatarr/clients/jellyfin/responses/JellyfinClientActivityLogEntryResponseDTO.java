package com.rattatarr.rattatarr.clients.jellyfin.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JellyfinClientActivityLogEntryResponseDTO(
        @JsonProperty("Id") Long id,
        @JsonProperty("Name") String name,
        @JsonProperty("Overview") String overview,
        @JsonProperty("ShortOverview") String shortOverview,
        @JsonProperty("Type") String type,
        @JsonProperty("ItemId") String itemId,
        @JsonProperty("Date") String date,
        @JsonProperty("UserId") String userId,
        @JsonProperty("Severity") String severity
) implements Serializable {
}
