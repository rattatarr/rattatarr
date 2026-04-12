package com.rattatarr.rattatarr.clients.jellyfin.requests.queries;

import java.util.List;

public record JellyfinItemsQuery(
        String parentId,
        Boolean recursive,
        Boolean isMovie,
        Boolean isSeries,
        List<String> filters,
        List<String> fields
) {
}
