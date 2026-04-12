package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.entities.Person;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface PeopleRepository extends BaseRepository<Person> {
    Optional<Person> findByTMDbId(String TMDbId);

    Set<Person> findAllByTMDbIdIn(Set<String> tmdbIds);
}
