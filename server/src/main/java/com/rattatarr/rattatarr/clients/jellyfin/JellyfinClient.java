package com.rattatarr.rattatarr.clients.jellyfin;

import com.rattatarr.rattatarr.clients.BaseClient;
import com.rattatarr.rattatarr.clients.Warmable;
import com.rattatarr.rattatarr.clients.jellyfin.requests.queries.JellyfinItemsQuery;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientPlaybackItemResponseDTO;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinClientUserResponseDTO;
import com.rattatarr.rattatarr.clients.jellyfin.responses.JellyfinSystemInfoResponseDTO;
import com.rattatarr.rattatarr.clients.jellyfin.responses.wrappers.JellyfinClientActivityLogEntriesWrapper;
import com.rattatarr.rattatarr.clients.jellyfin.responses.wrappers.JellyfinClientItemsWrapper;
import com.rattatarr.rattatarr.configs.RestClientProperties;
import com.rattatarr.rattatarr.exceptions.JellyfinClientExceptions;
import com.rattatarr.rattatarr.utils.URISanitizer;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Component
@NullMarked
public class JellyfinClient extends BaseClient<JellyfinClientExceptions> implements Warmable {

    private final JellyfinConfig config;

    public JellyfinClient(RestClient restClient, RestClientProperties properties, JellyfinConfig config) {
        super(restClient, properties);
        this.config = config;
    }

    @Override
    protected JellyfinClientExceptions mapException(RestClientResponseException e) {
        return new JellyfinClientExceptions(
                "Jellyfin API error: " + e.getMessage(),
                HttpStatus.BAD_GATEWAY,
                e.getStatusCode(),
                e.getResponseBodyAsString(),
                e
        );
    }

    @Override
    public boolean isConfigured() {
        return config.isConfigured();
    }

    @Override
    public void warmUp() {
        testConnection();
    }

    public boolean testConnection() {
        try {
            JellyfinSystemInfoResponseDTO response = executeGet(
                    config.buildUrl(URISanitizer.pathEnsureLeadingSlash("System/Info")),
                    JellyfinSystemInfoResponseDTO.class,
                    headers -> headers.set("Authorization", config.getAuthHeader())
            );
            return response.Id() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public JellyfinClientActivityLogEntriesWrapper getActivityLogEntries() {
        return getActivityLogEntries(0);
    }

    public JellyfinClientActivityLogEntriesWrapper getActivityLogEntries(int startIndex) {
        URI uri = UriComponentsBuilder
                .fromUriString(config.buildUrl(URISanitizer.pathEnsureLeadingSlash("System/ActivityLog/Entries")))
                .queryParam("limit", 50)
                .queryParam("StartIndex", startIndex)
                .build()
                .toUri();

        return executeGet(
                uri,
                JellyfinClientActivityLogEntriesWrapper.class,
                headers -> headers.set("Authorization", config.getAuthHeader())
        );
    }

    public JellyfinClientPlaybackItemResponseDTO getItemByIdForUser(String itemId, String userId) {
        URI uri = UriComponentsBuilder
                .fromUriString(config.buildUrl(URISanitizer.pathEnsureLeadingSlash("Items/" + itemId)))
                .queryParam("userId", userId)
                .build()
                .toUri();

        return executeGet(
                uri,
                JellyfinClientPlaybackItemResponseDTO.class,
                headers -> headers.set("Authorization", config.getAuthHeader())
        );
    }

    public List<JellyfinClientUserResponseDTO> getUsers() {
        return executeGet(
                config.buildUrl(URISanitizer.pathEnsureLeadingSlash("Users")),
                new ParameterizedTypeReference<List<JellyfinClientUserResponseDTO>>() {
                },
                headers -> headers.set("Authorization", config.getAuthHeader())
        );
    }

    public JellyfinClientItemsWrapper getItems(JellyfinItemsQuery query) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(
                config.buildUrl(URISanitizer.pathEnsureLeadingSlash("Items"))
        );

        if (query.parentId() != null) {
            builder = builder.queryParam("ParentId", query.parentId());
        }
        if (query.recursive() != null) {
            builder = builder.queryParam("Recursive", query.recursive().toString());
        }
        if (query.isMovie() != null) {
            builder = builder.queryParam("isMovie", query.isMovie().toString());
        }
        if (query.isSeries() != null) {
            builder = builder.queryParam("isSeries", query.isSeries().toString());
        }
        if (query.filters() != null && !query.filters().isEmpty()) {
            builder = builder.queryParam("Filters", String.join(",", query.filters()));
        }
        if (query.fields() != null && !query.fields().isEmpty()) {
            builder = builder.queryParam("Fields", String.join(",", query.fields()));
        }

        return executeGet(
                builder.build().toUri(),
                JellyfinClientItemsWrapper.class,
                headers -> headers.set("Authorization", config.getAuthHeader())
        );
    }
}
