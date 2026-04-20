package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.entities.MediaItemMetadata;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MediaItemMetadataResponseDTO(
        String description,
        String posterImageUrl,
        String backdropImageUrl,
        @Nullable Float imdbRating,
        @Nullable Integer rottenTomatoesRating
) implements Serializable {
    public static MediaItemMetadataResponseDTO fromEntity(MediaItemMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        return new MediaItemMetadataResponseDTO(
                metadata.description(),
                metadata.posterImageUrl(),
                metadata.backdropImageUrl(),
                metadata.imdbRating(),
                metadata.rottenTomatoesRating()
        );
    }
}
