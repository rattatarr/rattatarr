package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record YearRewindHighlightItemDTO(
        String title,
        String posterImageUrl,
        Float rating,
        Instant eventAt,
        String mediaType
) {}
