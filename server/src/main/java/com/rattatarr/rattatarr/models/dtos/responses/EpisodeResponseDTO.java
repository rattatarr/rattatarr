package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.entities.MediaEpisode;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EpisodeResponseDTO(
        UUID id,
        String jellyfinId,
        int episode,
        String title,
        int runtimeMinutes
) implements Serializable {
    public static EpisodeResponseDTO fromEntity(MediaEpisode mediaEpisode) {
        return new EpisodeResponseDTO(
                mediaEpisode.id(),
                mediaEpisode.jellyfinId(),
                mediaEpisode.episode(),
                mediaEpisode.title(),
                mediaEpisode.runtimeMinutes()
        );
    }

    public static List<EpisodeResponseDTO> fromEntities(Set<MediaEpisode> mediaEpisodes) {
        return mediaEpisodes.stream()
                .map(EpisodeResponseDTO::fromEntity)
                .toList();
    }
}
