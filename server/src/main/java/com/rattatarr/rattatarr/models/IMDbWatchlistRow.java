package com.rattatarr.rattatarr.models;

import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;

import java.time.Instant;

/**
 * Represents a row from an IMDb CSV export.
 * Used for importing user ratings from IMDb.
 */
public record IMDbWatchlistRow(
        String IMDbId, // From "Const" column (e.g., tt0468569)
        Float rating, // From "Your Rating" column (1.0-10.0)
        String itemType, // From "Title Type" column
        @Nullable Instant dateRated,// From "Date Rated" column (nullable
        MediaType mediaType// Derived media type (MOVIE or TV),

) {
    /**
     * Checks if this row represents a movie or TV series that can be imported.
     */
    public boolean isImportable() {
        return StringUtils.hasText(IMDbId)
                && rating != null
                && rating >= 1.0
                && rating <= 10.0
                && itemType != null
                && (itemType.equalsIgnoreCase("Movie")
                || itemType.toLowerCase().contains("series"));
    }
}
