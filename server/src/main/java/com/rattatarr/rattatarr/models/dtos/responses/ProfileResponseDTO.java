package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.utils.ZonedMapper;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProfileResponseDTO(
        UUID id,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        String name,
        String jellyfinId
) implements Serializable {

    public static ProfileResponseDTO fromEntity(Profile profile, ZoneId zoneId) {
        return new ProfileResponseDTO(
                profile.id(),
                ZonedMapper.toZonedDateTime(profile.createdAt(), zoneId),
                ZonedMapper.toZonedDateTime(profile.updatedAt(), zoneId),
                profile.name(),
                profile.jellyfinId()
        );
    }
}
