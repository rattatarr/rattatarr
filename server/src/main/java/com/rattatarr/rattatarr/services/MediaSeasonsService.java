package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbSeasonResponseDTO;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaSeason;
import com.rattatarr.rattatarr.repositories.MediaSeasonsRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@NullMarked
public class MediaSeasonsService extends BaseService<MediaSeason, MediaSeasonsRepository> {

    public MediaSeasonsService(MediaSeasonsRepository repository) {
        super(repository);
    }

    @Override
    protected void updateEntity(MediaSeason existing, MediaSeason updated) {
        existing.setTitle(updated.title());
        existing.setSeason(updated.season());
        if (updated.jellyfinId() != null) {
            existing.setJellyfinId(updated.jellyfinId());
        }
    }

    @Transactional(readOnly = true)
    public Optional<MediaSeason> findByMediaItemAndSeason(MediaItem mediaItem, Integer season) {
        return repository.findByMediaItemAndSeason(mediaItem, season);
    }

    @Transactional
    public void upsertBatchFromTMDb(List<TMDbSeasonResponseDTO> seasonDTOs, MediaItem mediaItem) {
        if (seasonDTOs.isEmpty()) {
            return;
        }

        List<MediaSeason> seasons = seasonDTOs.stream()
                .map(dto -> {
                    MediaSeason newSeason = new MediaSeason(
                            mediaItem,
                            null, // jellyfinId
                            dto.seasonNumber(),
                            dto.name(),
                            Set.of()
                    );
                    MediaSeason existing = findByMediaItemAndSeason(mediaItem, dto.seasonNumber())
                            .orElse(null);
                    return upsert(existing, newSeason);
                })
                .toList();

        logger.debug("Upserted {} seasons for media item {}", seasons.size(), mediaItem.title());
    }
}
