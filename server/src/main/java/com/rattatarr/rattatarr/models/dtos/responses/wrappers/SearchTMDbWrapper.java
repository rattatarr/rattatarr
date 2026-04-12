package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.dtos.responses.SearchTMDbResponseDTO;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SearchTMDbWrapper(
        List<SearchTMDbResponseDTO> results,
        Integer totalResults,
        Integer totalPages,
        Integer currentPage
) implements Serializable {
    public static SearchTMDbWrapper of(
            List<SearchTMDbResponseDTO> results,
            Integer totalResults,
            Integer totalPages,
            Integer currentPage
    ) {
        return new SearchTMDbWrapper(results, totalResults, totalPages, currentPage);
    }

}
