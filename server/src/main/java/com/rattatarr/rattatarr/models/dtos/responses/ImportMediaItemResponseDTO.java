package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import org.jspecify.annotations.NullMarked;

import java.io.Serializable;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ImportMediaItemResponseDTO(
    UUID id,
    String title,
    String TMDbId,
    MediaType mediaType
) implements Serializable {
    public static ImportMediaItemResponseDTO fromMediaItem(MediaItem mediaItem) {
        return new ImportMediaItemResponseDTO(
            mediaItem.id(),
            mediaItem.title(),
            mediaItem.TMDbId(),
            mediaItem.mediaType()
        );
    }
}
