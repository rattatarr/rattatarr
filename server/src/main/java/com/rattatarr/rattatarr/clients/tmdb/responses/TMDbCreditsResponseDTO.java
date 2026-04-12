package com.rattatarr.rattatarr.clients.tmdb.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TMDbCreditsResponseDTO(
        List<TMDbCreditCastMemberResponseDTO> cast,
        List<TMDbCreditCrewMemberResponseDTO> crew
) implements Serializable {
}
