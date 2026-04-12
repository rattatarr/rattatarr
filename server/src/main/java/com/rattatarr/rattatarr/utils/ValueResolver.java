package com.rattatarr.rattatarr.utils;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class ValueResolver {
    private ValueResolver() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static <T> T valueOrDefault(@Nullable T value, T defaultValue) {
        if (value == null || isEmptyString(value)) {
            return defaultValue;
        }
        return value;
    }

    public static boolean isEmptyString(Object value) {
        return value instanceof String str && str.isEmpty();
    }

    public static Integer runTimeTicksToMinutes(@Nullable Long runtimeTicks) {
        if (runtimeTicks == null) {
            return 0;
        }
        return Math.toIntExact(runtimeTicks / 10000000L / 60L);
    }

    /** Rounds a nullable {@code Double} to 2 decimal places. Returns {@code 0.0} if null. */
    public static double round2(@Nullable Double value) {
        if (value == null) return 0.0;
        return Math.round(value * 100.0) / 100.0;
    }

    /** Rounds a nullable {@code Float} to 2 decimal places. Returns {@code 0.0} if null. */
    public static double round2(@Nullable Float value) {
        if (value == null) return 0.0;
        return Math.round(value * 100.0) / 100.0;
    }
}
