package com.rattatarr.rattatarr.models.dtos.requests;

import com.rattatarr.rattatarr.models.MediaType;

import java.io.Serializable;

public record BrokenMediaItemsFiltersDTO(
        MediaType mediaType
) implements Serializable {
}
