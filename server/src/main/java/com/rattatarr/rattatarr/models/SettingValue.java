package com.rattatarr.rattatarr.models;

import org.jspecify.annotations.Nullable;

public record SettingValue(
        @Nullable
        String value,
        @Nullable
        String description
) {
}
