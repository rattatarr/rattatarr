package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.dtos.responses.GenreResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.PaginationMetadata;
import com.rattatarr.rattatarr.models.entities.Genre;
import org.springframework.data.domain.Page;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GenresWrapper(
        List<GenreResponseDTO> genres,
        PaginationMetadata pagination
) {
    public static GenresWrapper fromPage(Page<Genre> genres) {
        return new GenresWrapper(
                genres.stream()
                        .map(GenreResponseDTO::fromEntity)
                        .toList(),
                new PaginationMetadata(
                        genres.getNumber(),
                        genres.getSize(),
                        genres.getTotalElements(),
                        genres.getTotalPages(),
                        genres.isFirst(),
                        genres.isLast(),
                        genres.hasNext(),
                        genres.hasPrevious()
                )
        );
    }

    public static GenresWrapper fromList(List<Genre> genres) {
        return new GenresWrapper(
                genres.stream()
                        .map(GenreResponseDTO::fromEntity)
                        .toList(),
                null
        );
    }
}
