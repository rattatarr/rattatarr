package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.entities.MediaItemMetadata;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MediaItemMetadataResponseDTO(
        String description,
        String posterImageUrl,
        String backdropImageUrl
) implements Serializable {
    public static MediaItemMetadataResponseDTO fromEntity(MediaItemMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        return new MediaItemMetadataResponseDTO(
                metadata.description(),
                metadata.posterImageUrl(),
                metadata.backdropImageUrl()
        );
    }
}
