package com.rattatarr.rattatarr.configs;

import com.rattatarr.rattatarr.exceptions.BaseRattatarrExceptions;
import com.rattatarr.rattatarr.exceptions.JellyfinClientExceptions;
import com.rattatarr.rattatarr.models.dtos.responses.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {
    @Value("${rattatarr.debug.stacktrace:false}")
    private boolean includeStackTrace;

    private List<String> getStackTrace(@NonNull Throwable throwable) {
        if (!includeStackTrace) {
            return null;
        }
        return Arrays.stream(throwable.getStackTrace())
                .map(StackTraceElement::toString)
                .toList();
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.METHOD_NOT_ALLOWED,
                "MethodNotAllowedError - HTTP Method Not Supported",
                "The HTTP method " + ex.getMethod() + " is not supported for this endpoint.",
                request.getRequestURI(),
                getStackTrace(ex)
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST,
                "BadRequestError - Malformed JSON Request",
                "The request body is missing or malformed. Please ensure the JSON is correctly formatted.",
                request.getRequestURI(),
                getStackTrace(ex)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND,
                "NotFoundError - Resource Not Found",
                "The requested resource was not found: " + request.getRequestURI(),
                request.getRequestURI(),
                getStackTrace(ex)
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }


    @ExceptionHandler(BaseRattatarrExceptions.class)
    public ResponseEntity<ErrorResponse> handleBaseRattatarrException(BaseRattatarrExceptions ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                ex.status(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                request.getRequestURI(),
                getStackTrace(ex)
        );
        return ResponseEntity.status(errorResponse.status()).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.UNPROCESSABLE_CONTENT,
                "ValidationError - Invalid Request Data",
                String.join("; ", errors),
                request.getRequestURI(),
                getStackTrace(ex)
        );


        return ResponseEntity.status(errorResponse.status()).body(errorResponse);
    }

    /**
     * This acts as a catch-all for JPA exceptions, specifically to handle unique constraint violations.
     * Defensive programming should be used and specific exceptions should be caught where possible.
     * This is last line of defense for database-related exceptions.
     * <p>
     * SQLite unique constraint violation messages contain "SQLITE_CONSTRAINT_UNIQUE".
     * If PostgreSQL or another database is used in the future, this logic will need to be updated
     *
     * @param ex      The JpaSystemException thrown.
     * @param request The HttpServletRequest that resulted in the exception.
     * @return A ResponseEntity containing an ErrorResponse with details about the exception.
     */
    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ErrorResponse> handleJpaSystemException(JpaSystemException ex,
                                                                  HttpServletRequest request) {
        Throwable rootCause = ex.getRootCause();

        String message = ex.getMessage();
        if (rootCause != null && rootCause.getMessage() != null) {
            message = rootCause.getMessage();
        }

        if (message != null && message.contains("SQLITE_CONSTRAINT_UNIQUE")) {
            String constraintInfo = message.substring(message.indexOf("UNIQUE constraint failed:") + 26).trim();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    new ErrorResponse(
                            Instant.now(),
                            HttpStatus.CONFLICT,
                            "ConflictError - Unique Constraint Violation",
                            "Unique constraint violation on: " + constraintInfo,
                            request.getRequestURI(),
                            getStackTrace(ex)
                    )
            );
        }

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "JpaSystemException - Database Error",
                ex.getMessage(),
                request.getRequestURI(),
                getStackTrace(ex)
        );
        return ResponseEntity.status(errorResponse.status()).body(errorResponse);
    }


    @ExceptionHandler(JellyfinClientExceptions.class)
    public ResponseEntity<ErrorResponse> handleJellyfinClientException(JellyfinClientExceptions ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                ex.status(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                request.getRequestURI(),
                getStackTrace(ex)
        );
        return ResponseEntity.status(errorResponse.status()).body(errorResponse);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "InternalServerError - Unhandled Exception",
                ex.getMessage(),
                request.getRequestURI(),
                getStackTrace(ex)
        );
        return ResponseEntity.status(errorResponse.status()).body(errorResponse);
    }
}
