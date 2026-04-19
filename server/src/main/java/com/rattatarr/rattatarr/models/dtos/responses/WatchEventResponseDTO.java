package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.WatchEventType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemRating;
import com.rattatarr.rattatarr.models.entities.WatchEvent;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WatchEventResponseDTO(
        UUID id,
        Instant watchedAt,
        WatchEventType eventType,
        @Nullable Integer positionSeconds,
        @Nullable MovieResponseDTO movie,
        @Nullable ShowResponseDTO show,
        @Nullable Integer seasonNumber,
        @Nullable Integer episodeNumber,
        @Nullable String episodeTitle
) {
    public static WatchEventResponseDTO from(WatchEvent event, MediaItem mediaItem, @Nullable MediaItemRating rating) {
        Integer seasonNumber = event.mediaSeason() != null ? event.mediaSeason().season() : null;
        Integer episodeNumber = event.episode() != null ? event.episode().episode() : null;
        String episodeTitle = event.episode() != null ? event.episode().title() : null;

        MovieResponseDTO movie = null;
        ShowResponseDTO show = null;

        if (mediaItem.mediaType() == MediaType.MOVIE) {
            movie = MovieResponseDTO.fromEntity(mediaItem, rating, false);
        } else {
            show = ShowResponseDTO.fromEntity(mediaItem, rating, false);
        }

        return new WatchEventResponseDTO(
                event.id(),
                event.watchedAt(),
                event.eventType(),
                event.positionSeconds(),
                movie,
                show,
                seasonNumber,
                episodeNumber,
                episodeTitle
        );
    }
}
