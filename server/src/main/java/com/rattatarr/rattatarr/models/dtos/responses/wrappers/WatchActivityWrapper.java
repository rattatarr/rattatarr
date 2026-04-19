package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.dtos.responses.PaginationMetadata;
import com.rattatarr.rattatarr.models.dtos.responses.WatchEventResponseDTO;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WatchActivityWrapper(
        List<WatchEventResponseDTO> events,
        PaginationMetadata pagination
) implements Serializable {

    public static WatchActivityWrapper fromPage(Page<WatchEventResponseDTO> page) {
        return new WatchActivityWrapper(
                page.getContent(),
                new PaginationMetadata(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.isFirst(),
                        page.isLast(),
                        page.hasNext(),
                        page.hasPrevious()
                )
        );
    }
}
