package com.rattatarr.rattatarr.models.dtos.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GenericResponseDTO(
        String message,
        HttpStatus status,
        Object data
) implements Serializable {
    public static GenericResponseDTO success(String message, @Nullable Object data) {
        return new GenericResponseDTO(message, HttpStatus.OK, data);
    }

    public static GenericResponseDTO accepted(String message, @Nullable Object data) {
        return new GenericResponseDTO(message, HttpStatus.ACCEPTED, data);

    }

    public static GenericResponseDTO failure(String message) {
        return new GenericResponseDTO(message, HttpStatus.BAD_REQUEST, null);
    }

    public static GenericResponseDTO unknownError(String message) {
        return new GenericResponseDTO(message, HttpStatus.INTERNAL_SERVER_ERROR, null);

    }
}
