package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.entities.MediaSeasonMetadata;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaSeasonMetadataRepository extends BaseRepository<MediaSeasonMetadata> {
    Optional<MediaSeasonMetadata> findByMediaSeasonId(UUID mediaSeasonId);
}
