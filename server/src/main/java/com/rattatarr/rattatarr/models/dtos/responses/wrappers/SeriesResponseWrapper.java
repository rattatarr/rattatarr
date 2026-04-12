package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.dtos.responses.PaginationMetadata;
import com.rattatarr.rattatarr.models.dtos.responses.ShowResponseDTO;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SeriesResponseWrapper(
        List<ShowResponseDTO> series,
        PaginationMetadata pagination
) implements Serializable {

    public static SeriesResponseWrapper fromPage(Page<ShowResponseDTO> series) {
        return new SeriesResponseWrapper(
                series.getContent(),
                new PaginationMetadata(
                        series.getNumber(),
                        series.getSize(),
                        series.getTotalElements(),
                        series.getTotalPages(),
                        series.isFirst(),
                        series.isLast(),
                        series.hasNext(),
                        series.hasPrevious()
                )
        );
    }

    public static SeriesResponseWrapper fromList(List<ShowResponseDTO> series) {
        return new SeriesResponseWrapper(series, null);
    }
}
