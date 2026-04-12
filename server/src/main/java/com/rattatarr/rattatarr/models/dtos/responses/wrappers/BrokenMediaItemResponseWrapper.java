package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.dtos.responses.BrokenMediaItemResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.PaginationMetadata;
import com.rattatarr.rattatarr.models.entities.BrokenMediaItem;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BrokenMediaItemResponseWrapper(
        List<BrokenMediaItemResponseDTO> movies,
        PaginationMetadata pagination
) implements Serializable {

    public static BrokenMediaItemResponseWrapper fromPage(Page<BrokenMediaItem> movies) {
        return new BrokenMediaItemResponseWrapper(
                movies.stream()
                        .map(BrokenMediaItemResponseDTO::fromEntity)
                        .toList(),
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

    public static BrokenMediaItemResponseWrapper fromList(List<BrokenMediaItem> movies) {
        return new BrokenMediaItemResponseWrapper(
                movies.stream()
                        .map(BrokenMediaItemResponseDTO::fromEntity)
                        .toList(),
                null
        );
    }
}
