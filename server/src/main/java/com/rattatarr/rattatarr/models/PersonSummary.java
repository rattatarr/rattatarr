package com.rattatarr.rattatarr.models;

public record PersonSummary(
        String name,
        Long count,
        Double averageRating
) {
}
