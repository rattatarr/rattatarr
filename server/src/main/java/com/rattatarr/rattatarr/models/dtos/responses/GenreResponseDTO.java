package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.entities.Genre;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GenreResponseDTO(
        UUID id,
        String name
) implements Serializable {
    public static GenreResponseDTO fromEntity(Genre genre) {
        return new GenreResponseDTO(
                genre.id(),
                genre.name()
        );
    }

    public static Set<GenreResponseDTO> fromEntities(Set<Genre> genres) {
        return genres.stream()
                .map(GenreResponseDTO::fromEntity)
                .collect(java.util.stream.Collectors.toSet());
    }
}
