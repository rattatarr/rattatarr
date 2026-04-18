package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.rattatarr.rattatarr.models.dtos.responses.BackgroundJobResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.PaginationMetadata;
import org.springframework.data.domain.Page;

import java.util.List;

public record BackgroundJobsWrapper(
        List<BackgroundJobResponseDTO> jobs,
        PaginationMetadata pagination
) {
    public static BackgroundJobsWrapper fromPage(Page<BackgroundJobResponseDTO> page) {
        return new BackgroundJobsWrapper(
                page.getContent(),
                new PaginationMetadata(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.isFirst(),
                        page.isLast(),
                        page.hasNext(),
                        page.hasPrevious()
                )
        );
    }
}
