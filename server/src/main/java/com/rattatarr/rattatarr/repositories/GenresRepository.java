package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.entities.Genre;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface GenresRepository extends BaseRepository<Genre> {
    Optional<Genre> findByName(String name);

    Set<Genre> findAllByNameIn(Set<String> names);
}
