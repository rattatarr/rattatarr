package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.rattatarr.rattatarr.clients.radarr.responses.RadarrMovieResponseDTO;

import java.util.List;

public record RadarrMoviesWrapper(List<RadarrMovieResponseDTO> movies) {
    public static RadarrMoviesWrapper fromList(List<RadarrMovieResponseDTO> movies) {
        return new RadarrMoviesWrapper(movies);
    }
}
