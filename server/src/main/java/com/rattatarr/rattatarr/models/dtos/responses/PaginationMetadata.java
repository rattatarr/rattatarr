package com.rattatarr.rattatarr.models.dtos.responses;

import java.io.Serializable;

public record PaginationMetadata(
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isFirst,
        boolean isLast,
        boolean hasNext,
        boolean hasPrevious
) implements Serializable {
}
