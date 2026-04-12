package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.entities.MediaItemCast;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CastMemberResponseDTO(
        UUID id,
        PersonResponseDTO person,
        String character,
        Integer order
) implements Serializable {
    public static CastMemberResponseDTO fromEntity(MediaItemCast cast) {
        PersonResponseDTO personDto = null;

        var person = cast.person();
        if (person != null && Hibernate.isInitialized(person)) {
            personDto = PersonResponseDTO.fromEntity(person);
        }

        return new CastMemberResponseDTO(
                cast.id(),
                personDto,
                cast.character(),
                cast.order()
        );
    }

    public static List<CastMemberResponseDTO> fromEntities(Set<MediaItemCast> cast) {
        return cast.stream()
                .map(CastMemberResponseDTO::fromEntity)
                .toList();
    }
}
