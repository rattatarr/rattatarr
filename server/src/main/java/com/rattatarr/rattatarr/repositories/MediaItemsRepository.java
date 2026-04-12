package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.entities.MediaItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaItemsRepository extends BaseRepository<MediaItem> {
    Optional<MediaItem> findByJellyfinId(String jellyfinId);

    @Query("SELECT mi FROM MediaItem mi LEFT JOIN FETCH mi.genres")
    List<MediaItem> findAllWithGenres();

    Optional<MediaItem> findByTMDbId(String TMDbId);
}
