package com.rattatarr.rattatarr.models.dtos.responses;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void testRecordConstructor() {
        // Given
        Instant timestamp = Instant.now();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String error = "ValidationError";
        String message = "Invalid input";
        String path = "/api/v1/profiles";
        List<String> stackTrace = Arrays.asList("line1", "line2");

        // When
        ErrorResponse response = new ErrorResponse(timestamp, status, error, message, path, stackTrace);

        // Then
        assertEquals(timestamp, response.timestamp());
        assertEquals(status, response.status());
        assertEquals(error, response.error());
        assertEquals(message, response.message());
        assertEquals(path, response.path());
        assertEquals(stackTrace, response.stackTrace());
    }

    @Test
    void testWithNullStackTrace() {
        // Given
        Instant timestamp = Instant.now();
        HttpStatus status = HttpStatus.NOT_FOUND;

        // When
        ErrorResponse response = new ErrorResponse(
                timestamp,
                status,
                "NotFoundError",
                "Resource not found",
                "/api/v1/profiles/123",
                null
        );

        // Then
        assertNotNull(response);
        assertNull(response.stackTrace());
    }

    @Test
    void testWithEmptyStackTrace() {
        // Given
        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error",
                "Message",
                "/path",
                List.of()
        );

        // Then
        assertNotNull(response.stackTrace());
        assertTrue(response.stackTrace().isEmpty());
    }

    @Test
    void testRecordEquality() {
        // Given
        Instant timestamp = Instant.now();
        ErrorResponse response1 = new ErrorResponse(
                timestamp,
                HttpStatus.BAD_REQUEST,
                "Error",
                "Message",
                "/path",
                null
        );
        ErrorResponse response2 = new ErrorResponse(
                timestamp,
                HttpStatus.BAD_REQUEST,
                "Error",
                "Message",
                "/path",
                null
        );

        // Then
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testRecordInequality() {
        // Given
        Instant timestamp = Instant.now();
        ErrorResponse response1 = new ErrorResponse(
                timestamp,
                HttpStatus.BAD_REQUEST,
                "Error",
                "Message1",
                "/path",
                null
        );
        ErrorResponse response2 = new ErrorResponse(
                timestamp,
                HttpStatus.BAD_REQUEST,
                "Error",
                "Message2",
                "/path",
                null
        );

        // Then
        assertNotEquals(response1, response2);
    }

    @Test
    void testRecordToString() {
        // Given
        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND,
                "NotFoundError",
                "Profile not found",
                "/api/v1/profiles/123",
                null
        );

        // When
        String toString = response.toString();

        // Then
        assertTrue(toString.contains("NOT_FOUND"));
        assertTrue(toString.contains("Profile not found"));
        assertTrue(toString.contains("/api/v1/profiles/123"));
    }

    @Test
    void testDifferentHttpStatuses() {
        // Given
        Instant now = Instant.now();

        // When
        ErrorResponse badRequest = new ErrorResponse(now, HttpStatus.BAD_REQUEST, "err", "msg", "/", null);
        ErrorResponse notFound = new ErrorResponse(now, HttpStatus.NOT_FOUND, "err", "msg", "/", null);
        ErrorResponse unauthorized = new ErrorResponse(now, HttpStatus.UNAUTHORIZED, "err", "msg", "/", null);
        ErrorResponse forbidden = new ErrorResponse(now, HttpStatus.FORBIDDEN, "err", "msg", "/", null);
        ErrorResponse conflict = new ErrorResponse(now, HttpStatus.CONFLICT, "err", "msg", "/", null);
        ErrorResponse serverError = new ErrorResponse(now, HttpStatus.INTERNAL_SERVER_ERROR, "err", "msg", "/", null);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, badRequest.status());
        assertEquals(HttpStatus.NOT_FOUND, notFound.status());
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorized.status());
        assertEquals(HttpStatus.FORBIDDEN, forbidden.status());
        assertEquals(HttpStatus.CONFLICT, conflict.status());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, serverError.status());
    }

    @Test
    void testWithLongStackTrace() {
        // Given
        List<String> longStackTrace = Arrays.asList(
                "at com.rattatarr.Class1.method1(Class1.java:10)",
                "at com.rattatarr.Class2.method2(Class2.java:20)",
                "at com.rattatarr.Class3.method3(Class3.java:30)",
                "at com.rattatarr.Class4.method4(Class4.java:40)"
        );

        // When
        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error",
                "Something went wrong",
                "/api/test",
                longStackTrace
        );

        // Then
        assertNotNull(response.stackTrace());
        assertEquals(4, response.stackTrace().size());
        assertTrue(response.stackTrace().getFirst().contains("Class1.method1"));
    }
}
