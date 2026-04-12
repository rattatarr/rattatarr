package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.responses.TMDbCreditsResponseDTO;
import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.models.dtos.requests.PeopleFiltersDTO;
import com.rattatarr.rattatarr.models.dtos.responses.PersonResponseDTO;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.Person;
import com.rattatarr.rattatarr.repositories.PeopleRepository;
import com.rattatarr.rattatarr.services.helpers.MediaItemViewHelper;
import com.rattatarr.rattatarr.specifications.GenericSpecifications;
import com.rattatarr.rattatarr.specifications.PeopleSpecifications;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@NullMarked
public class PeopleService extends BaseService<Person, PeopleRepository> {

    private final MediaItemCastService mediaItemCastService;
    private final MediaItemCrewService mediaItemCrewService;
    private final MediaItemViewHelper mediaItemViewHelper;

    public PeopleService(PeopleRepository repository, MediaItemCastService mediaItemCastService, MediaItemCrewService mediaItemCrewService, MediaItemViewHelper mediaItemViewHelper) {
        super(repository);
        this.mediaItemCastService = mediaItemCastService;
        this.mediaItemCrewService = mediaItemCrewService;
        this.mediaItemViewHelper = mediaItemViewHelper;
    }

    @Override
    protected void updateEntity(Person existing, Person updated) {
        existing.setName(updated.name());
        existing.setProfilePathUrl(updated.profilePathUrl());
    }

    @Transactional(readOnly = true)
    public PersonResponseDTO getPersonById(String id, String profileSize) {
        Person person;
        try {
            UUID uuid = UUID.fromString(id);
            person = findById(uuid)
                    .orElseThrow(() -> new CommonExceptions.ResourceNotFoundExceptions("Person", uuid));
        } catch (IllegalArgumentException e) {
            person = repository.findByTMDbId(id)
                    .orElseThrow(() -> new CommonExceptions.ResourceNotFoundExceptions("Person with TMDb ID not found: " + id));
        }
        PersonResponseDTO dto = PersonResponseDTO.fromEntity(person);
        if (dto.profilePathUrl() != null) {
            return new PersonResponseDTO(
                    dto.id(),
                    dto.name(),
                    dto.TMDbId(),
                    mediaItemViewHelper.buildUrlFromPath(dto.profilePathUrl(), profileSize)
            );
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<PersonResponseDTO> filterPeople(PeopleFiltersDTO filters, Pageable pageable) {
        Specification<Person> spec = Specification.allOf(
                GenericSpecifications.notDeleted(),
                PeopleSpecifications.nameLike(filters.name())
        );

        return repository.findAll(spec, pageable)
                .map(person -> {
                    PersonResponseDTO dto = PersonResponseDTO.fromEntity(person);
                    if (dto != null && dto.profilePathUrl() != null) {
                        return new PersonResponseDTO(
                                dto.id(),
                                dto.name(),
                                dto.TMDbId(),
                                mediaItemViewHelper.buildUrlFromPath(dto.profilePathUrl(), filters.profileSize())
                        );
                    }
                    return dto;
                });
    }

    @Transactional
    public void upsertPeopleFromCredits(TMDbCreditsResponseDTO credits, MediaItem mediaItem, Boolean forceRefresh) {
        for (var castMember : credits.cast()) {
            Person person = upsert(castMember.personTMDbId(), castMember.name(), castMember.profilePath(), forceRefresh);
            mediaItemCastService.upsert(mediaItem, castMember, person, forceRefresh);
        }

        for (var crewMember : credits.crew()) {
            Person person = upsert(crewMember.personTMDbId(), crewMember.name(), crewMember.profilePath(), forceRefresh);
            mediaItemCrewService.upsert(mediaItem, crewMember, person, forceRefresh);
        }
    }

    @Transactional
    public Person upsert(String TMDbId, String name, String profilePath, Boolean forceRefresh) {
        return repository.findByTMDbId(TMDbId)
                .map(existingPerson -> {
                    if (!forceRefresh) {
                        return existingPerson;
                    }
                    logger.info("Updating existing Person with TMDb ID: {}. Name: {}", TMDbId, name);
                    existingPerson.setName(name);
                    existingPerson.setProfilePathUrl(profilePath);
                    return repository.save(existingPerson);
                })
                .orElseGet(() -> {
                    Person newPerson = new Person(name, TMDbId, profilePath);
                    return repository.save(newPerson);
                });
    }

    @Transactional
    public void upsertBatchFromTMDbDTOs(TMDbCreditsResponseDTO credits, MediaItem mediaItem) {
        Set<String> allTMDbIds = new HashSet<>();
        credits.cast().forEach(castMember -> allTMDbIds.add(castMember.personTMDbId()));
        credits.crew().forEach(crewMember -> allTMDbIds.add(crewMember.personTMDbId()));

        if (allTMDbIds.isEmpty()) {
            return;
        }

        Set<Person> existingPeople = repository.findAllByTMDbIdIn(allTMDbIds);
        Map<String, Person> existingMap = existingPeople.stream()
                .collect(Collectors.toMap(Person::TMDbId, Function.identity()));

        Set<Person> peopleToInsert = new HashSet<>();

        credits.cast().forEach(castMember -> {
            if (!existingMap.containsKey(castMember.personTMDbId())) {
                peopleToInsert.add(new Person(
                        castMember.name(),
                        castMember.personTMDbId(),
                        castMember.profilePath()
                ));
            }
        });

        credits.crew().forEach(crewMember -> {
            if (!existingMap.containsKey(crewMember.personTMDbId())) {
                peopleToInsert.add(new Person(
                        crewMember.name(),
                        crewMember.personTMDbId(),
                        crewMember.profilePath()
                ));
            }
        });

        // Defensive deduplication
        peopleToInsert.removeIf(person -> existingMap.containsKey(person.TMDbId()));

        if (!peopleToInsert.isEmpty()) {
            repository.saveAll(peopleToInsert);
        }

        Map<String, Person> allPeopleMap = new HashMap<>(existingMap);
        peopleToInsert.forEach(person -> allPeopleMap.put(person.TMDbId(), person));

        mediaItemCastService.upsertBatchFromTMDb(credits.cast(), mediaItem, allPeopleMap);
        mediaItemCrewService.upsertBatchFromTMDb(credits.crew(), mediaItem, allPeopleMap);
    }
}
