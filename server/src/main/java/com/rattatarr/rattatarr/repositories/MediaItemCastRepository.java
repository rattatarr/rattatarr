package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemCast;
import com.rattatarr.rattatarr.models.entities.Person;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaItemCastRepository extends BaseRepository<MediaItemCast> {
    List<MediaItemCast> findByMediaItemId(UUID mediaItemId);

    List<MediaItemCast> findByMediaItemIdIn(List<UUID> mediaItemIds);

    List<MediaItemCast> findByPersonId(UUID personId);

    List<MediaItemCast> findByCharacter(String character);

    Optional<MediaItemCast> findByMediaItemAndPersonAndCharacter(MediaItem mediaItem, Person person, String character);
}
