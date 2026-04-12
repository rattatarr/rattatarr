package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.entities.MediaSeason;
import com.rattatarr.rattatarr.models.entities.MediaSeasonRating;
import org.hibernate.Hibernate;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.util.List;
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
    public static SeasonResponseDTO fromEntity(MediaSeason mediaSeason) {
        return new SeasonResponseDTO(
                mediaSeason.id(),
                mediaSeason.jellyfinId(),
                mediaSeason.season(),
                mediaSeason.title(),
                Hibernate.isInitialized(mediaSeason.metadata()) ? MediaSeasonMetadataResponseDTO.fromEntity(mediaSeason.metadata()) : null,
                Hibernate.isInitialized(mediaSeason.episodes()) && !ObjectUtils.isEmpty(mediaSeason.episodes())
                        ? EpisodeResponseDTO.fromEntities(mediaSeason.episodes())
                        : null,
                Hibernate.isInitialized(mediaSeason.ratings()) && !mediaSeason.ratings().isEmpty()
                        ? mediaSeason.ratings().stream().findFirst().map(MediaSeasonRating::rating).orElse(null)
                        : null
        );
    }

    public static List<SeasonResponseDTO> fromEntities(Set<MediaSeason> mediaSeasons) {
        return mediaSeasons.stream()
                .map(SeasonResponseDTO::fromEntity)
                .toList();
    }
}
