package com.rattatarr.rattatarr.clients.jellyfin.requests.queries;

import java.util.ArrayList;
import java.util.List;

public final class JellyfinItemsQueryBuilder {

    private final List<String> filters = new ArrayList<>();
    private final List<String> fields = new ArrayList<>();
    private String parentId;
    private Boolean recursive;
    private Boolean isMovie;
    private Boolean isSeries;

    public static JellyfinItemsQueryBuilder builder() {
        return new JellyfinItemsQueryBuilder();
    }

    public JellyfinItemsQueryBuilder parentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public JellyfinItemsQueryBuilder filter(String filter) {
        this.filters.add(filter);
        return this;
    }

    public JellyfinItemsQueryBuilder field(String field) {
        this.fields.add(field);
        return this;
    }

    public JellyfinItemsQueryBuilder recursive(Boolean recursive) {
        this.recursive = recursive;
        return this;
    }

    public JellyfinItemsQueryBuilder isMovie(Boolean isMovie) {
        this.isMovie = isMovie;
        return this;
    }

    public JellyfinItemsQueryBuilder isSeries(Boolean isSeries) {
        this.isSeries = isSeries;
        return this;
    }

    public JellyfinItemsQuery build() {
        return new JellyfinItemsQuery(parentId, recursive, isMovie, isSeries, List.copyOf(filters), List.copyOf(fields));
    }
}
