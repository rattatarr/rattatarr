package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbEpisodeResponseDTO;
import com.rattatarr.rattatarr.models.entities.MediaEpisode;
import com.rattatarr.rattatarr.models.entities.MediaSeason;
import com.rattatarr.rattatarr.repositories.MediaEpisodesRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@NullMarked
public class MediaEpisodesService extends BaseService<MediaEpisode, MediaEpisodesRepository> {

    public MediaEpisodesService(MediaEpisodesRepository repository) {
        super(repository);
    }

    @Override
    protected void updateEntity(MediaEpisode existing, MediaEpisode updated) {
        existing.setTitle(updated.title());
        existing.setEpisode(updated.episode());
        if (updated.runtimeMinutes() != null) {
            existing.setRuntimeMinutes(updated.runtimeMinutes());
        }
        if (updated.jellyfinId() != null) {
            existing.setJellyfinId(updated.jellyfinId());
        }
    }

    @Transactional(readOnly = true)
    public Optional<MediaEpisode> findByMediaSeasonAndEpisode(MediaSeason mediaSeason, Integer episode) {
        return repository.findByMediaSeasonAndEpisode(mediaSeason, episode);
    }

    @Transactional
    public void upsertBatchFromTMDb(List<TMDbEpisodeResponseDTO> episodeDTOs, MediaSeason mediaSeason) {
        if (episodeDTOs.isEmpty()) {
            return;
        }

        List<MediaEpisode> episodes = episodeDTOs.stream()
                .map(dto -> {
                    MediaEpisode newEpisode = new MediaEpisode(
                            mediaSeason,
                            dto.episodeNumber(),
                            dto.name(),
                            dto.runtime() != null ? dto.runtime() : 0
                    );
                    MediaEpisode existing = findByMediaSeasonAndEpisode(mediaSeason, dto.episodeNumber())
                            .orElse(null);
                    return upsert(existing, newEpisode);
                })
                .toList();

        logger.debug("Upserted {} episodes for season {}", episodes.size(), mediaSeason.season());
    }
}
