package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.dtos.responses.SearchTMDbResponseDTO;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SearchGroupTMDbWrapper(
        List<SearchTMDbResponseDTO> movies,
        List<SearchTMDbResponseDTO> series
) implements Serializable {
    public static SearchGroupTMDbWrapper of(
            List<SearchTMDbResponseDTO> movies,
            List<SearchTMDbResponseDTO> series
    ) {
        return new SearchGroupTMDbWrapper(movies, series);
    }

}
