package com.rattatarr.rattatarr.configs;

import com.rattatarr.rattatarr.exceptions.BaseRattatarrExceptions;
import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.dtos.responses.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Test
    void testHandleBaseRattatarrException() {
        // Given
        BaseRattatarrExceptions exception = new ProfilesExceptions.ProfileNotFoundExceptions(UUID.randomUUID());

        // When
        ResponseEntity<ErrorResponse> response = handler.handleBaseRattatarrException(exception, request);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, response.getBody().status());
        assertTrue(response.getBody().message().contains("not found"));
        assertEquals("/api/v1/test", response.getBody().path());
    }

    @Test
    void testHandleBaseRattatarrExceptionWithStackTrace() {
        // Given
        ReflectionTestUtils.setField(handler, "includeStackTrace", true);
        BaseRattatarrExceptions exception = new CommonExceptions.InvalidRequestExceptions("Invalid data");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleBaseRattatarrException(exception, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().stackTrace());
        assertFalse(response.getBody().stackTrace().isEmpty());
    }

    @Test
    void testHandleBaseRattatarrExceptionWithoutStackTrace() {
        // Given
        ReflectionTestUtils.setField(handler, "includeStackTrace", false);
        BaseRattatarrExceptions exception = new CommonExceptions.InvalidRequestExceptions("Invalid data");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleBaseRattatarrException(exception, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().stackTrace());
    }

    @Test
    void testHandleValidationException() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("object", "name", "Name is required");
        FieldError fieldError2 = new FieldError("object", "email", "Email is invalid");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception, request);

        // Then
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("Name is required"));
        assertTrue(response.getBody().message().contains("Email is invalid"));
    }

    @Test
    void testHandleJpaSystemExceptionWithUniqueConstraint() {
        // Given
        String constraintMessage = "SQLITE_CONSTRAINT_UNIQUE: UNIQUE constraint failed: profiles.name";
        Exception rootCause = new RuntimeException(constraintMessage);
        JpaSystemException exception = mock(JpaSystemException.class);
        when(exception.getRootCause()).thenReturn(rootCause);
        when(exception.getMessage()).thenReturn("SQL Error");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleJpaSystemException(exception, request);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().message().contains("Unique constraint violation"));
    }

    @Test
    void testHandleJpaSystemExceptionWithoutUniqueConstraint() {
        // Given
        JpaSystemException exception = mock(JpaSystemException.class);
        when(exception.getRootCause()).thenReturn(null);
        when(exception.getMessage()).thenReturn("Generic database error");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleJpaSystemException(exception, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Generic database error", response.getBody().message());
    }

    @Test
    void testHandleAllGenericException() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = (ResponseEntity<ErrorResponse>) handler.handleAll(exception, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unexpected error", response.getBody().message());
        assertEquals("/api/v1/test", response.getBody().path());
    }

    @Test
    void testHandleUnauthorizedException() {
        // Given
        BaseRattatarrExceptions exception = new CommonExceptions.UnauthorizedExceptions("Invalid token");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleBaseRattatarrException(exception, request);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid token", response.getBody().message());
    }

    @Test
    void testHandleForbiddenException() {
        // Given
        BaseRattatarrExceptions exception = new CommonExceptions.ForbiddenExceptions("Access denied");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleBaseRattatarrException(exception, request);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access denied", response.getBody().message());
    }

    @Test
    void testHandleConflictException() {
        // Given
        BaseRattatarrExceptions exception = new CommonExceptions.ConflictExceptions("Resource already exists");

        // When
        ResponseEntity<ErrorResponse> response = handler.handleBaseRattatarrException(exception, request);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Resource already exists", response.getBody().message());
    }
}
