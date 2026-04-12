package com.rattatarr.rattatarr.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class BaseRattatarrExceptionTest {

    @Test
    void testExceptionWithMessageAndStatus() {
        // Given/When
        TestExceptions exception = new TestExceptions("Test error", HttpStatus.BAD_REQUEST);

        // Then
        assertEquals("Test error", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.status());
    }

    @Test
    void testExceptionWithMessageCauseAndStatus() {
        // Given
        Throwable cause = new RuntimeException("Root cause");

        // When
        TestExceptions exception = new TestExceptions("Test error", cause, HttpStatus.INTERNAL_SERVER_ERROR);

        // Then
        assertEquals("Test error", exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.status());
    }

    @Test
    void testExceptionStatus() {
        // Given/When
        TestExceptions notFound = new TestExceptions("Not found", HttpStatus.NOT_FOUND);
        TestExceptions unauthorized = new TestExceptions("Unauthorized", HttpStatus.UNAUTHORIZED);
        TestExceptions forbidden = new TestExceptions("Forbidden", HttpStatus.FORBIDDEN);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, notFound.status());
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorized.status());
        assertEquals(HttpStatus.FORBIDDEN, forbidden.status());
    }

    @Test
    void testExceptionIsRuntimeException() {
        // Given/When
        TestExceptions exception = new TestExceptions("Test", HttpStatus.BAD_REQUEST);

        // Then
        assertInstanceOf(RuntimeException.class, exception);
    }

    // Test implementation of BaseRattatarrException
    private static class TestExceptions extends BaseRattatarrExceptions {
        protected TestExceptions(String message, HttpStatus status) {
            super(message, status);
        }

        protected TestExceptions(String message, Throwable cause, HttpStatus status) {
            super(message, cause, status);
        }
    }
}
