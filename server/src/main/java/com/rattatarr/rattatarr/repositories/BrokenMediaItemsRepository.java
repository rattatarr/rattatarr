package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.BrokenMediaItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrokenMediaItemsRepository extends BaseRepository<BrokenMediaItem> {
    Optional<BrokenMediaItem> findByJellyfinId(String jellyfinId);

    Page<BrokenMediaItem> findByMediaType(MediaType mediaType, Pageable pageable);
}
