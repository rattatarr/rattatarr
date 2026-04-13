package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemCrew;
import com.rattatarr.rattatarr.models.entities.Person;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaItemCrewRepository extends BaseRepository<MediaItemCrew> {
    List<MediaItemCrew> findByMediaItemId(UUID mediaItemId);

    List<MediaItemCrew> findByMediaItemIdIn(List<UUID> mediaItemIds);

    List<MediaItemCrew> findByPersonId(UUID personId);

    List<MediaItemCrew> findByJob(String job);

    Optional<MediaItemCrew> findByMediaItemAndPersonAndJobAndDepartment(MediaItem mediaItem, Person person, String job, String department);
}
