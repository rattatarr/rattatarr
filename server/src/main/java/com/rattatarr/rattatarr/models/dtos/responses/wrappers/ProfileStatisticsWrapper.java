package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.rattatarr.rattatarr.models.dtos.responses.ProfileStatisticsResponseDTO;

import java.io.Serializable;

public record ProfileStatisticsWrapper(
        ProfileStatisticsResponseDTO statistics
) implements Serializable {
    public static ProfileStatisticsWrapper fromDTO(ProfileStatisticsResponseDTO statistics) {
        return new ProfileStatisticsWrapper(statistics);
    }
}
