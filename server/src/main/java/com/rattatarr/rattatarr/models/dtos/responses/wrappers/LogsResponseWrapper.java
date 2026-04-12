package com.rattatarr.rattatarr.models.dtos.responses.wrappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rattatarr.rattatarr.models.LogEvent;
import com.rattatarr.rattatarr.models.dtos.responses.PaginationMetadata;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LogsResponseWrapper(
        List<LogEvent> logs,
        PaginationMetadata pagination
) implements Serializable {
    public static LogsResponseWrapper fromPage(Page<LogEvent> page) {
        return new LogsResponseWrapper(
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

    public static LogsResponseWrapper fromList(List<LogEvent> logs) {
        return new LogsResponseWrapper(logs, null);
    }
}
