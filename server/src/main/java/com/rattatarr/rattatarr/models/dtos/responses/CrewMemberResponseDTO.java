package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.entities.MediaItemCrew;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CrewMemberResponseDTO(
        UUID id,
        PersonResponseDTO person,
        String job,
        String department,
        String profilePathUrl
) implements Serializable {
    public static CrewMemberResponseDTO fromEntity(MediaItemCrew crew) {
        PersonResponseDTO personDto = null;

        var person = crew.person();
        if (person != null && Hibernate.isInitialized(person)) {
            personDto = PersonResponseDTO.fromEntity(person);
        }

        return new CrewMemberResponseDTO(
                crew.id(),
                personDto,
                crew.job(),
                crew.department(),
                personDto != null ? personDto.profilePathUrl() : null
        );
    }

    public static List<CrewMemberResponseDTO> fromEntities(Set<MediaItemCrew> crew) {
        return crew.stream()
                .map(CrewMemberResponseDTO::fromEntity)
                .toList();
    }
}
