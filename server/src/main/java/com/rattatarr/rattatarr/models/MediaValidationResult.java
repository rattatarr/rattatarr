package com.rattatarr.rattatarr.models;

public record MediaValidationResult(boolean isValid, String missingFields) {
}
