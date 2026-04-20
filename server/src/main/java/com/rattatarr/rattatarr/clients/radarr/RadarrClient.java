package com.rattatarr.rattatarr.clients.radarr;

import com.rattatarr.rattatarr.clients.BaseClient;
import com.rattatarr.rattatarr.clients.Warmable;
import com.rattatarr.rattatarr.clients.radarr.responses.RadarrMovieResponseDTO;
import com.rattatarr.rattatarr.configs.RestClientProperties;
import com.rattatarr.rattatarr.exceptions.RadarrClientExceptions;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
@NullMarked
public class RadarrClient extends BaseClient<RadarrClientExceptions> implements Warmable {
    private final RadarrConfig config;

    public RadarrClient(RestClient restClient, RestClientProperties properties, RadarrConfig config) {
        super(restClient, properties);
        this.config = config;
    }

    @Override
    protected RadarrClientExceptions mapException(RestClientResponseException e) {
        return new RadarrClientExceptions(
                "Radarr API error: " + e.getMessage(),
                HttpStatus.BAD_GATEWAY,
                e.getStatusCode(),
                e.getResponseBodyAsString(),
                e
        );
    }

    @Override
    public void warmUp() {
        testConnection();
    }

    @Override
    public boolean isConfigured() {
        return config.isConfigured();
    }

    public boolean testConnection() {
        try {
            getMovies(120);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<RadarrMovieResponseDTO> getMovies() {
        return getMovies(null);
    }

    public List<RadarrMovieResponseDTO> getMovies(@Nullable Integer tmdbId) {
        // Not sure yet if we can set /api/v3 at top level or if there will be cases where we need an old version.
        // Looking at seer that uses v1
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(config.buildUrl("/api/v3/movie"));
        if (tmdbId != null) {
            builder = builder.queryParam("tmdbId", tmdbId);
        }
        return executeGet(
                builder.build().toUri(),
                new ParameterizedTypeReference<List<RadarrMovieResponseDTO>>() {
                },
                headers -> headers.set("X-Api-Key", config.getAuthHeader())
        );
    }
}
