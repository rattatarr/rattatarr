package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.entities.MediaSeason;
import org.hibernate.Hibernate;
import org.jspecify.annotations.Nullable;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SeasonResponseDTO(
        UUID id,
        String jellyfinId,
        int season,
        String title,
        MediaSeasonMetadataResponseDTO metadata,
        List<EpisodeResponseDTO> episodes,
        Float myRating
) implements Serializable {
    public static SeasonResponseDTO fromEntity(MediaSeason mediaSeason, @Nullable Float rating) {
        return new SeasonResponseDTO(
                mediaSeason.id(),
                mediaSeason.jellyfinId(),
                mediaSeason.season(),
                mediaSeason.title(),
                Hibernate.isInitialized(mediaSeason.metadata()) ? MediaSeasonMetadataResponseDTO.fromEntity(mediaSeason.metadata()) : null,
                Hibernate.isInitialized(mediaSeason.episodes()) && !ObjectUtils.isEmpty(mediaSeason.episodes())
                        ? EpisodeResponseDTO.fromEntities(mediaSeason.episodes())
                        : null,
                rating
        );
    }

    public static SeasonResponseDTO fromEntity(MediaSeason mediaSeason) {
        return fromEntity(mediaSeason, null);
    }

    public static List<SeasonResponseDTO> fromEntities(Set<MediaSeason> mediaSeasons, Map<UUID, Float> seasonRatings) {
        return mediaSeasons.stream()
                .sorted(Comparator.comparing(MediaSeason::season))
                .map(season -> fromEntity(season, seasonRatings.get(season.id())))
                .toList();
    }

    public static List<SeasonResponseDTO> fromEntities(Set<MediaSeason> mediaSeasons) {
        return fromEntities(mediaSeasons, new HashMap<>());
    }
}
