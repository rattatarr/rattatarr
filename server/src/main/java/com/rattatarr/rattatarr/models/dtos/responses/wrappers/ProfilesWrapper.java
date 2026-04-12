package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.dtos.responses.PaginationMetadata;
import com.rattatarr.rattatarr.models.dtos.responses.ProfileResponseDTO;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProfilesWrapper(
        List<ProfileResponseDTO> profiles,
        PaginationMetadata pagination
) implements Serializable {
    public static ProfilesWrapper fromPage(Page<ProfileResponseDTO> profiles) {
        return new ProfilesWrapper(
                profiles.getContent(),
                new PaginationMetadata(
                        profiles.getNumber(),
                        profiles.getSize(),
                        profiles.getTotalElements(),
                        profiles.getTotalPages(),
                        profiles.isFirst(),
                        profiles.isLast(),
                        profiles.hasNext(),
                        profiles.hasPrevious()
                ));
    }

    public static ProfilesWrapper fromList(List<ProfileResponseDTO> profiles) {
        return new ProfilesWrapper(
                profiles,
                null
        );
    }
}
