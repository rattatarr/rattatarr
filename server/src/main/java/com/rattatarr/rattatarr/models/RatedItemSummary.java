package com.rattatarr.rattatarr.models;

public record RatedItemSummary(
        String title,
        String mediaType,
        Float rating
) {
}
