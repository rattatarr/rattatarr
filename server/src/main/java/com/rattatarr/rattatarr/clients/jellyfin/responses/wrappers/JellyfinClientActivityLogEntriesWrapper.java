package com.rattatarr.rattatarr.clients.jellyfin.responses.wrappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientActivityLogEntryResponseDTO;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JellyfinClientActivityLogEntriesWrapper(
        @JsonProperty("Items") List<JellyfinClientActivityLogEntryResponseDTO> items,
        @JsonProperty("TotalRecordCount") Integer totalRecordCount,
        @JsonProperty("StartIndex") Integer startIndex
) implements Serializable {
}
