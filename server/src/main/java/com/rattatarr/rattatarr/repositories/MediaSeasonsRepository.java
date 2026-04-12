package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaSeason;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MediaSeasonsRepository extends BaseRepository<MediaSeason> {
    Optional<MediaSeason> findByMediaItemAndSeason(MediaItem mediaItem, Integer season);
}
