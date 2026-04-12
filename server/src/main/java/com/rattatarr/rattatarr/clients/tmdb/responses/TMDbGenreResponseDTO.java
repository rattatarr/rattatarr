package com.rattatarr.rattatarr.clients.tmdb.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TMDbGenreResponseDTO(
        Integer id,
        String name
) implements Serializable {
}
