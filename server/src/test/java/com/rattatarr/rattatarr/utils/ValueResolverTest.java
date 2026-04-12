package com.rattatarr.rattatarr.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

class ValueResolverTest {

    @Test
    void constructor_shouldThrowUnsupportedOperationException() {
        // When & Then
        var exception = assertThrows(InvocationTargetException.class, () -> {
            var constructor = ValueResolver.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });

        assertEquals(UnsupportedOperationException.class, exception.getCause().getClass());
    }

    @Test
    void valueOrDefault_shouldReturnValueWhenNotNull() {
        // Given
        String value = "test";
        String defaultValue = "default";

        // When
        String result = ValueResolver.valueOrDefault(value, defaultValue);

        // Then
        assertEquals("test", result);
    }

    @Test
    void valueOrDefault_shouldReturnDefaultWhenNull() {
        // Given
        String value = null;
        String defaultValue = "default";

        // When
        String result = ValueResolver.valueOrDefault(value, defaultValue);

        // Then
        assertEquals("default", result);
    }

    @Test
    void valueOrDefault_shouldReturnDefaultWhenEmptyString() {
        // Given
        String value = "";
        String defaultValue = "default";

        // When
        String result = ValueResolver.valueOrDefault(value, defaultValue);

        // Then
        assertEquals("default", result);
    }

    @Test
    void valueOrDefault_shouldWorkWithNonStringTypes() {
        // Given
        Integer value = 42;
        Integer defaultValue = 0;

        // When
        Integer result = ValueResolver.valueOrDefault(value, defaultValue);

        // Then
        assertEquals(42, result);
    }

    @Test
    void valueOrDefault_shouldReturnDefaultForNullNonString() {
        // Given
        Integer value = null;
        Integer defaultValue = 0;

        // When
        Integer result = ValueResolver.valueOrDefault(value, defaultValue);

        // Then
        assertEquals(0, result);
    }

    @Test
    void isEmptyString_shouldReturnTrueForEmptyString() {
        // Given
        String value = "";

        // When
        boolean result = ValueResolver.isEmptyString(value);

        // Then
        assertTrue(result);
    }

    @Test
    void isEmptyString_shouldReturnFalseForNonEmptyString() {
        // Given
        String value = "test";

        // When
        boolean result = ValueResolver.isEmptyString(value);

        // Then
        assertFalse(result);
    }

    @Test
    void isEmptyString_shouldReturnFalseForNonString() {
        // Given
        Integer value = 42;

        // When
        boolean result = ValueResolver.isEmptyString(value);

        // Then
        assertFalse(result);
    }

    @Test
    void isEmptyString_shouldReturnFalseForNull() {
        // Given
        Object value = null;

        // When
        boolean result = ValueResolver.isEmptyString(value);

        // Then
        assertFalse(result);
    }

    @Test
    void runTimeTicksToMinutes_shouldConvertTicksToMinutes() {
        // Given - 60 minutes worth of ticks (60 * 60 * 10000000)
        Long runtimeTicks = 36000000000L;

        // When
        Integer result = ValueResolver.runTimeTicksToMinutes(runtimeTicks);

        // Then
        assertEquals(60, result);
    }

    @Test
    void runTimeTicksToMinutes_shouldReturnZeroForNull() {
        // Given
        Long runtimeTicks = null;

        // When
        Integer result = ValueResolver.runTimeTicksToMinutes(runtimeTicks);

        // Then
        assertEquals(0, result);
    }

    @Test
    void runTimeTicksToMinutes_shouldHandleZeroTicks() {
        // Given
        Long runtimeTicks = 0L;

        // When
        Integer result = ValueResolver.runTimeTicksToMinutes(runtimeTicks);

        // Then
        assertEquals(0, result);
    }

    @Test
    void runTimeTicksToMinutes_shouldHandleSmallValues() {
        // Given - 1 minute worth of ticks
        Long runtimeTicks = 600000000L;

        // When
        Integer result = ValueResolver.runTimeTicksToMinutes(runtimeTicks);

        // Then
        assertEquals(1, result);
    }

    @Test
    void runTimeTicksToMinutes_shouldHandleLargeValues() {
        // Given - 120 minutes (2 hours)
        Long runtimeTicks = 72000000000L;

        // When
        Integer result = ValueResolver.runTimeTicksToMinutes(runtimeTicks);

        // Then
        assertEquals(120, result);
    }
}
