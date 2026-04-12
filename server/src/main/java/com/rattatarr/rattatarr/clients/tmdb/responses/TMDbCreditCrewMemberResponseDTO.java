package com.rattatarr.rattatarr.clients.tmdb.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TMDbCreditCrewMemberResponseDTO(
        String name,
        String department,
        String job,
        @JsonProperty("profile_path") String profilePath,
        @JsonProperty("id") String personTMDbId
) implements Serializable {
}
