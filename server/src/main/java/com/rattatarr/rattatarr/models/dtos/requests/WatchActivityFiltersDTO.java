package com.rattatarr.rattatarr.models.dtos.requests;

import com.rattatarr.rattatarr.models.MediaType;

import java.io.Serializable;
import java.util.UUID;

public record WatchActivityFiltersDTO(
        UUID profileId,
        String startDate,
        String endDate,
        MediaType mediaType,
        String posterSize,
        String backdropSize
) implements Serializable {
}
