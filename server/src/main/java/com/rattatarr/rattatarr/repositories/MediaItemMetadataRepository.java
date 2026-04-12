package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.entities.MediaItemMetadata;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaItemMetadataRepository extends BaseRepository<MediaItemMetadata> {
    Optional<MediaItemMetadata> findByMediaItemId(UUID mediaItemId);
}
