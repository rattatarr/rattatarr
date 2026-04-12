package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbFindItemResponseDTO;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SearchTMDbResponseDTO(
        Integer id,
        String title,
        String releaseDate,
        String posterPath,
        String backdropPath,
        String description
) implements Serializable {
    public static SearchTMDbResponseDTO fromTMDbResponse(TMDbFindItemResponseDTO dto) {
        return new SearchTMDbResponseDTO(
                dto.id(),
                dto.title(),
                dto.releaseDate(),
                dto.posterPath(),
                dto.backdropPath(),
                dto.description()
        );
    }

    public static SearchTMDbResponseDTO of(
            Integer id,
            String title,
            String releaseDate,
            String posterPath,
            String backdropPath,
            String description
    ) {
        return new SearchTMDbResponseDTO(
                id,
                title,
                releaseDate,
                posterPath,
                backdropPath,
                description
        );
    }
}
