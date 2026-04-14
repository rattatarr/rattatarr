package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.entities.WatchEvent;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface WatchEventsRepository extends BaseRepository<WatchEvent> {
    boolean existsByJellyfinLogId(Long jellyfinLogId);

    @Query("SELECT w.jellyfinLogId FROM WatchEvent w WHERE w.jellyfinLogId IN :jellyfinLogIds")
    Set<Long> findExistingJellyfinLogIds(@Param("jellyfinLogIds") Collection<Long> jellyfinLogIds);
}
