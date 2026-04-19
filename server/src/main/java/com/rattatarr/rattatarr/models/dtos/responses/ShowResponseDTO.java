package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemRating;
import org.hibernate.Hibernate;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ShowResponseDTO(
        UUID id,
        String jellyfinId,
        String title,
        String TMDbId,
        String IMDbId,
        Integer productionYear,
        Integer runtimeMinutes,
        MediaItemMetadataResponseDTO metadata,
        Set<GenreResponseDTO> genres,
        List<SeasonResponseDTO> seasons,
        List<CastMemberResponseDTO> cast,
        List<CrewMemberResponseDTO> crew,
        Float myRating
) implements Serializable {
    public static ShowResponseDTO fromEntity(MediaItem mediaItem) {
        return fromEntity(mediaItem, false);
    }

    public static ShowResponseDTO fromEntity(MediaItem mediaItem, boolean includeCredits) {
        return fromEntity(mediaItem, null, includeCredits);
    }

    public static ShowResponseDTO fromEntity(MediaItem mediaItem, @Nullable MediaItemRating rating, boolean includeCredits) {
        return fromEntity(mediaItem, rating, includeCredits, Map.of());
    }

    public static ShowResponseDTO fromEntity(MediaItem mediaItem, @Nullable MediaItemRating rating, boolean includeCredits, Map<UUID, Float> seasonRatings) {
        return new ShowResponseDTO(
                mediaItem.id(),
                mediaItem.jellyfinId(),
                mediaItem.title(),
                mediaItem.TMDbId(),
                mediaItem.IMDbId(),
                mediaItem.productionYear(),
                mediaItem.runtimeMinutes(),
                Hibernate.isInitialized(mediaItem.metadata()) ? MediaItemMetadataResponseDTO.fromEntity(mediaItem.metadata()) : null,
                GenreResponseDTO.fromEntities(mediaItem.genres()),
                Hibernate.isInitialized(mediaItem.seasons()) && !mediaItem.seasons().isEmpty()
                        ? SeasonResponseDTO.fromEntities(mediaItem.seasons(), seasonRatings)
                        : null,
                includeCredits && Hibernate.isInitialized(mediaItem.cast()) && mediaItem.cast() != null && !mediaItem.cast().isEmpty()
                        ? CastMemberResponseDTO.fromEntities(mediaItem.cast())
                        : null,
                includeCredits && Hibernate.isInitialized(mediaItem.crew()) && mediaItem.crew() != null && !mediaItem.crew().isEmpty()
                        ? CrewMemberResponseDTO.fromEntities(mediaItem.crew())
                        : null,
                rating != null ? rating.rating() : null
        );
    }

    public static List<ShowResponseDTO> fromEntities(Set<MediaItem> mediaItems) {
        return mediaItems.stream()
                .map(ShowResponseDTO::fromEntity)
                .toList();
    }
}
