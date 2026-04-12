package com.rattatarr.rattatarr.clients.tmdb.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TMDbConfigurationResponseDTO(Images images) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Images(String base_url) {
    }
}
