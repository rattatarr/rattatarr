package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.WatchEventType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.WatchEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface WatchEventsRepository extends BaseRepository<WatchEvent> {
    boolean existsByJellyfinLogId(Long jellyfinLogId);

    boolean existsByProfile_IdAndMediaItem_IdAndEpisodeIsNullAndEventType(
            UUID profileId, UUID mediaItemId, WatchEventType eventType);

    boolean existsByProfile_IdAndEpisode_IdAndEventType(
            UUID profileId, UUID episodeId, WatchEventType eventType);

    @Query("SELECT w.jellyfinLogId FROM WatchEvent w WHERE w.jellyfinLogId IN :jellyfinLogIds")
    Set<Long> findExistingJellyfinLogIds(@Param("jellyfinLogIds") Collection<Long> jellyfinLogIds);

    @Query("""
            SELECT w.mediaItem FROM WatchEvent w
            WHERE w.profile.id = :profileId AND w.eventType = :eventType
            GROUP BY w.mediaItem
            ORDER BY MAX(w.watchedAt) DESC
            """)
    List<MediaItem> findRecentWatchedMediaItems(
            @Param("profileId") UUID profileId,
            @Param("eventType") WatchEventType eventType,
            Pageable pageable);
}
