package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.rattatarr.rattatarr.models.dtos.responses.YearRewindResponseDTO;

import java.io.Serializable;

public record YearRewindWrapper(
        YearRewindResponseDTO rewind
) implements Serializable {
    public static YearRewindWrapper fromDTO(YearRewindResponseDTO dto) {
        return new YearRewindWrapper(dto);
    }
}
