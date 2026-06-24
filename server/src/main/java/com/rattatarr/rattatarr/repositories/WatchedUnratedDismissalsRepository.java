package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.entities.WatchedUnratedDismissal;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WatchedUnratedDismissalsRepository extends BaseRepository<WatchedUnratedDismissal> {

    Optional<WatchedUnratedDismissal> findByProfileIdAndMediaItemId(UUID profileId, UUID mediaItemId);
}
