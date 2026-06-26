package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemRating;
import org.hibernate.Hibernate;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MovieResponseDTO(
        UUID id,
        String jellyfinId,
        String title,
        String TMDbId,
        String IMDbId,
        Integer productionYear,
        Integer runtimeMinutes,
        MediaItemMetadataResponseDTO metadata,
        Set<GenreResponseDTO> genres,
        List<CastMemberResponseDTO> cast,
        List<CrewMemberResponseDTO> crew,
        Float myRating,
        ReviewResponseDTO review
) implements Serializable {
    public static MovieResponseDTO fromEntity(MediaItem mediaItem) {
        return fromEntity(mediaItem, false);
    }

    public static MovieResponseDTO fromEntity(MediaItem mediaItem, boolean includeCredits) {
        return fromEntity(mediaItem, null, includeCredits);
    }

    public static MovieResponseDTO fromEntity(MediaItem mediaItem, @Nullable MediaItemRating rating, boolean includeCredits) {
        return new MovieResponseDTO(
                mediaItem.id(),
                mediaItem.jellyfinId(),
                mediaItem.title(),
                mediaItem.TMDbId(),
                mediaItem.IMDbId(),
                mediaItem.productionYear(),
                mediaItem.runtimeMinutes(),
                Hibernate.isInitialized(mediaItem.metadata()) ? MediaItemMetadataResponseDTO.fromEntity(mediaItem.metadata()) : null,
                GenreResponseDTO.fromEntities(mediaItem.genres()),
                includeCredits && Hibernate.isInitialized(mediaItem.cast()) && mediaItem.cast() != null && !mediaItem.cast().isEmpty()
                        ? CastMemberResponseDTO.fromEntities(mediaItem.cast())
                        : null,
                includeCredits && Hibernate.isInitialized(mediaItem.crew()) && mediaItem.crew() != null && !mediaItem.crew().isEmpty()
                        ? CrewMemberResponseDTO.fromEntities(mediaItem.crew())
                        : null,
                rating != null ? rating.rating() : null,
                ReviewResponseDTO.fromEntity(rating)
        );
    }
}
