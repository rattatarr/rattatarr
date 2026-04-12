package com.rattatarr.rattatarr.models;

import org.jspecify.annotations.NullMarked;

@NullMarked
public enum IMDbWatchlistColumn {
    POSITION("Position"),
    CONST("Const"),
    CREATED("Created"),
    MODIFIED("Modified"),
    DESCRIPTION("Description"),
    TITLE("Title"),
    ORIGINAL_TITLE("Original Title"),
    URL("URL"),
    TITLE_TYPE("Title Type"),
    IMDB_RATING("IMDb Rating"),
    RUNTIME_MINS("Runtime (mins)"),
    YEAR("Year"),
    GENRES("Genres"),
    NUM_VOTES("Num Votes"),
    RELEASE_DATE("Release Date"),
    DIRECTORS("Directors"),
    YOUR_RATING("Your Rating"),
    DATE_RATED("Date Rated");

    private final String columnName;

    IMDbWatchlistColumn(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Returns the actual column header name from IMDb CSV.
     */
    @Override
    public String toString() {
        return columnName;
    }
}
