package com.rattatarr.rattatarr.clients.jellyfin.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JellyfinClientPlaybackItemUserDataResponseDTO(
        @JsonProperty("PlaybackPositionTicks") Long playbackPositionTicks,
        @JsonProperty("Played") Boolean played
) implements Serializable {
}
