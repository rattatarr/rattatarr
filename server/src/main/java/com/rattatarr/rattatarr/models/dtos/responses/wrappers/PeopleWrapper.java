package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.dtos.responses.PersonResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.PaginationMetadata;
import com.rattatarr.rattatarr.models.entities.Person;
import org.springframework.data.domain.Page;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PeopleWrapper(
        List<PersonResponseDTO> people,
        PaginationMetadata pagination
) {
    public static PeopleWrapper fromPage(Page<PersonResponseDTO> people) {
        return new PeopleWrapper(
                people.getContent(),
                new PaginationMetadata(
                        people.getNumber(),
                        people.getSize(),
                        people.getTotalElements(),
                        people.getTotalPages(),
                        people.isFirst(),
                        people.isLast(),
                        people.hasNext(),
                        people.hasPrevious()
                )
        );
    }

    public static PeopleWrapper fromList(List<Person> people) {
        return new PeopleWrapper(
                people.stream()
                        .map(PersonResponseDTO::fromEntity)
                        .toList(),
                null
        );
    }
}
