package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.entities.MediaEpisode;
import com.rattatarr.rattatarr.models.entities.MediaSeason;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaEpisodesRepository extends BaseRepository<MediaEpisode> {
    Optional<MediaEpisode> findByMediaSeasonAndEpisode(MediaSeason mediaSeason, Integer episode);

    Optional<MediaEpisode> findByJellyfinId(String jellyfinId);

}
