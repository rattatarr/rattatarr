package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.entities.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfilesRepository extends BaseRepository<Profile> {
    Optional<Profile> findByJellyfinId(String jellyfinId);

    List<Profile> findByJellyfinIdIsNotNull();
}
