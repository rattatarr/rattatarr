package com.rattatarr.rattatarr.configs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiVersionTest {

    @Test
    void testAnnotationPresent() {
        // Given
        @ApiVersion("v1")
        class TestController {
        }

        // When
        boolean hasAnnotation = TestController.class.isAnnotationPresent(ApiVersion.class);

        // Then
        assertTrue(hasAnnotation);
    }

    @Test
    void testAnnotationValueV1() {
        // Given
        @ApiVersion("v1")
        class TestController {
        }

        // When
        ApiVersion annotation = TestController.class.getAnnotation(ApiVersion.class);

        // Then
        assertNotNull(annotation);
        assertEquals("v1", annotation.value());
    }

    @Test
    void testAnnotationValueV2() {
        // Given
        @ApiVersion("v2")
        class TestController {
        }

        // When
        ApiVersion annotation = TestController.class.getAnnotation(ApiVersion.class);

        // Then
        assertNotNull(annotation);
        assertEquals("v2", annotation.value());
    }

    @Test
    void testDefaultValue() {
        // Given
        @ApiVersion
        class TestController {
        }

        // When
        ApiVersion annotation = TestController.class.getAnnotation(ApiVersion.class);

        // Then
        assertNotNull(annotation);
        assertEquals("v1", annotation.value());
    }

    @Test
    void testAnnotationNotPresentOnClassWithoutAnnotation() {
        // Given
        class TestController {
        }

        // When
        boolean hasAnnotation = TestController.class.isAnnotationPresent(ApiVersion.class);

        // Then
        assertFalse(hasAnnotation);
    }
}
