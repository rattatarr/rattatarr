package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.dtos.responses.MovieResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.PaginationMetadata;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MoviesResponseWrapper(
        List<MovieResponseDTO> movies,
        PaginationMetadata pagination
) implements Serializable {

    public static MoviesResponseWrapper fromPage(Page<MovieResponseDTO> movies) {
        return new MoviesResponseWrapper(
                movies.getContent(),
                new PaginationMetadata(
                        movies.getNumber(),
                        movies.getSize(),
                        movies.getTotalElements(),
                        movies.getTotalPages(),
                        movies.isFirst(),
                        movies.isLast(),
                        movies.hasNext(),
                        movies.hasPrevious()
                )
        );
    }

    public static MoviesResponseWrapper fromList(List<MovieResponseDTO> movies) {
        return new MoviesResponseWrapper(movies, null);
    }
}
