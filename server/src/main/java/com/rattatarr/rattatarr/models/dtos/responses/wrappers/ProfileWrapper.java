package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.rattatarr.rattatarr.models.dtos.responses.ProfileResponseDTO;

import java.io.Serializable;

public record ProfileWrapper(
        ProfileResponseDTO profile
) implements Serializable {
    public static ProfileWrapper fromDTO(ProfileResponseDTO profile) {
        return new ProfileWrapper(profile);
    }
}
