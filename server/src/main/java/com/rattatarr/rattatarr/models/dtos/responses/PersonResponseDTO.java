package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.entities.Person;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PersonResponseDTO(
        UUID id,
        String name,
        String TMDbId,
        String profilePathUrl
) implements Serializable {
    public static PersonResponseDTO fromEntity(Person person) {
        if (person == null) return null;
        return new PersonResponseDTO(
                person.id(),
                person.name(),
                person.TMDbId(),
                person.profilePathUrl()
        );
    }

    public static List<PersonResponseDTO> fromEntities(Set<Person> people) {
        return people.stream()
                .map(PersonResponseDTO::fromEntity)
                .toList();
    }
}
