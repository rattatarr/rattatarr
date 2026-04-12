package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.entities.BrokenMediaItem;

import java.io.Serializable;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BrokenMediaItemResponseDTO(
        UUID id,
        String title,
        String jellyfinId,
        String TMDbId,
        String IMDbId,
        Integer productionYear,
        String missingFields,
        UUID resolvedMediaItemId,
        boolean resolved)
        implements Serializable {
    public static BrokenMediaItemResponseDTO fromEntity(BrokenMediaItem brokenMediaItem) {
        return new BrokenMediaItemResponseDTO(
                brokenMediaItem.id(),
                brokenMediaItem.title(),
                brokenMediaItem.jellyfinId(),
                brokenMediaItem.TMDbId(),
                brokenMediaItem.IMDbId(),
                brokenMediaItem.productionYear(),
                brokenMediaItem.missingFields(),
                brokenMediaItem.resolvedMediaItem() != null
                        ? brokenMediaItem.resolvedMediaItem().id()
                        : null,
                brokenMediaItem.resolved());
    }
}
