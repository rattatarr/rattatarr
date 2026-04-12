package com.rattatarr.rattatarr.clients.jellyfin.responses.wrappers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientItemResponseDTO;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JellyfinClientItemsWrapper(
        @JsonProperty("Items") List<JellyfinClientItemResponseDTO> items
) implements Serializable {
}
