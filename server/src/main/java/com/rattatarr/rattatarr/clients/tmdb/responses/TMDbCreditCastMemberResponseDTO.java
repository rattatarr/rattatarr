package com.rattatarr.rattatarr.clients.tmdb.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TMDbCreditCastMemberResponseDTO(
        String name,
        String character,
        @JsonProperty("profile_path") String profilePath,
        @JsonProperty("id") String personTMDbId,
        Integer order
) implements Serializable {
}
