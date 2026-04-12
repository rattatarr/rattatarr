package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.entities.MediaSeasonMetadata;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MediaSeasonMetadataResponseDTO(
        Integer episodeCount,
        String posterImageUrl,
        String airDate
) implements Serializable {
    public static MediaSeasonMetadataResponseDTO fromEntity(MediaSeasonMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        return new MediaSeasonMetadataResponseDTO(
                metadata.episodeCount(),
                metadata.posterImageUrl(),
                metadata.airDate()
        );
    }
}
