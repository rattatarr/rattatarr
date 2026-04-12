package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbGenreResponseDTO;
import com.rattatarr.rattatarr.models.dtos.requests.GenresFiltersDTO;
import com.rattatarr.rattatarr.models.entities.Genre;
import com.rattatarr.rattatarr.repositories.GenresRepository;
import com.rattatarr.rattatarr.specifications.GenericSpecifications;
import com.rattatarr.rattatarr.specifications.GenreSpecifications;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@NullMarked
public class GenresService extends BaseService<Genre, GenresRepository> {
    private final ConcurrentHashMap<String, Genre> cache = new ConcurrentHashMap<>();

    public GenresService(GenresRepository repository) {
        super(repository);
    }

    @Override
    protected void updateEntity(Genre existing, Genre updated) {
    }

    @Transactional(readOnly = true)
    public Genre findOrCreateByName(String name) {
        return repository.findByName(name)
                .orElseGet(() -> repository.save(new Genre(name)));
    }

    public Genre findOrCreateByNameCached(String name) {
        return cache.computeIfAbsent(name, this::findOrCreateByName);
    }

    @Transactional(readOnly = true)
    public Page<Genre> filterGenres(GenresFiltersDTO filters, Pageable pageable) {
        Specification<Genre> spec = Specification.allOf(
                GenericSpecifications.notDeleted(),
                GenreSpecifications.nameLike(filters.name())
        );

        return repository.findAll(spec, pageable);
    }

    @Transactional
    public Set<Genre> upsertGenresFromTMDbDTOs(List<TMDbGenreResponseDTO> genreDTOs) {
        Set<String> genreNames = genreDTOs.stream()
                .map(TMDbGenreResponseDTO::name)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (genreNames.isEmpty()) {
            return Set.of();
        }

        Set<Genre> existingGenres = repository.findAllByNameIn(genreNames);
        Map<String, Genre> existingMap = existingGenres.stream()
                .collect(Collectors.toMap(Genre::name, Function.identity()));

        Set<Genre> genresToInsert = genreNames.stream()
                .filter(name -> !existingMap.containsKey(name))
                .map(Genre::new)
                .collect(Collectors.toSet());

        if (!genresToInsert.isEmpty()) {
            repository.saveAll(genresToInsert);
        }

        Set<Genre> result = new LinkedHashSet<>(existingGenres);
        result.addAll(genresToInsert);
        return result;
    }
}
