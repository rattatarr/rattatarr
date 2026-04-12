package com.rattatarr.rattatarr.clients.tmdb;

import com.rattatarr.rattatarr.clients.BaseClient;
import com.rattatarr.rattatarr.clients.Warmable;
import com.rattatarr.rattatarr.clients.tmdb.responses.*;
import com.rattatarr.rattatarr.configs.RestClientProperties;
import com.rattatarr.rattatarr.exceptions.TMDbClientExceptions;
import com.rattatarr.rattatarr.utils.URISanitizer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@NullMarked
public class TMDbClient extends BaseClient<TMDbClientExceptions> implements Warmable {
    private static final int MAX_SEASONS_PER_REQUEST = 19;
    private final static String TMDB_API_VERSION = "3";
    private final TMDbConfig config;

    public TMDbClient(RestClient restClient, RestClientProperties properties, TMDbConfig config) {
        super(restClient, properties);
        this.config = config;
    }

    @Override
    protected TMDbClientExceptions mapException(RestClientResponseException e) {
        return new TMDbClientExceptions(
                "TMDb API error: " + e.getMessage(),
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
            TMDbConfigurationResponseDTO response = executeGet(
                    config.buildUrl(URISanitizer.pathEnsureLeadingSlash(TMDB_API_VERSION + "/configuration")),
                    TMDbConfigurationResponseDTO.class,
                    headers -> headers.set("Authorization", config.getAuthHeader())
            );
            return response.images() != null && response.images().base_url() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public String getImageUrl(String path) {
        return getImageUrl(path, null);
    }

    public String getImageUrl(String path, @Nullable String size) {
        return config.getImageUrl(path, size);
    }

    public TMDbMovieResponseDTO findMovieById(String id) {
        return executeGet(
                config.buildUrl(URISanitizer.pathEnsureLeadingSlash(TMDB_API_VERSION + "/movie/" + id)),
                TMDbMovieResponseDTO.class,
                headers -> headers.set("Authorization", config.getAuthHeader())
        );
    }

    public TMDbMovieFullDetailsResponseDTO findMovieFullDetailsById(String id) {
        return executeGet(
                config.buildUrl(URISanitizer.pathEnsureLeadingSlash(TMDB_API_VERSION + "/movie/" + id + "?append_to_response=credits")),
                TMDbMovieFullDetailsResponseDTO.class,
                headers -> headers.set("Authorization", config.getAuthHeader())
        );
    }

    public TMDbShowResponseDTO findTVShowById(String id) {
        return executeGet(
                config.buildUrl(URISanitizer.pathEnsureLeadingSlash(TMDB_API_VERSION + "/tv/" + id)),
                TMDbShowResponseDTO.class,
                headers -> headers.set("Authorization", config.getAuthHeader())
        );
    }

    public TMDbShowFullDetailsResponseDTO findTVShowFullDetailsById(String id) {
        return findTVShowFullDetailsById(id, 1, MAX_SEASONS_PER_REQUEST);
    }

    public TMDbShowFullDetailsResponseDTO findTVShowFullDetailsById(String id, int startSeason, int endSeason) {
        StringBuilder appendParams = new StringBuilder("credits");
        for (int i = startSeason; i <= endSeason; i++) {
            appendParams.append(",season/").append(i);
        }

        return executeGet(
                config.buildUrl(URISanitizer.pathEnsureLeadingSlash(
                        TMDB_API_VERSION + "/tv/" + id + "?append_to_response=" + appendParams)),
                TMDbShowFullDetailsResponseDTO.class,
                headers -> headers.set("Authorization", config.getAuthHeader())
        );
    }

    public TMDbShowFullDetailsResponseDTO findTVShowAdditionalSeasons(String id, int startSeason, int endSeason) {
        StringBuilder appendParams = new StringBuilder();
        for (int i = startSeason; i <= endSeason; i++) {
            if (i > startSeason) {
                appendParams.append(",");
            }
            appendParams.append("season/").append(i);
        }

        return executeGet(
                config.buildUrl(URISanitizer.pathEnsureLeadingSlash(
                        TMDB_API_VERSION + "/tv/" + id + "?append_to_response=" + appendParams)),
                TMDbShowFullDetailsResponseDTO.class,
                headers -> headers.set("Authorization", config.getAuthHeader())
        );
    }

    public TMDbFindResponseDTO findByIMDbId(String imdbId) {
        return executeGet(
                config.buildUrl(URISanitizer.pathEnsureLeadingSlash(TMDB_API_VERSION + "/find/" + imdbId + "?external_source=imdb_id")),
                TMDbFindResponseDTO.class,
                headers -> headers.set("Authorization", config.getAuthHeader())
        );
    }

    public TMDbCreditsResponseDTO findMovieCreditsById(String id) {
        return executeGet(
                config.buildUrl(URISanitizer.pathEnsureLeadingSlash(TMDB_API_VERSION + "/movie/" + id + "/credits")),
                TMDbCreditsResponseDTO.class,
                headers -> headers.set("Authorization", config.getAuthHeader())
        );
    }

    public TMDbCreditsResponseDTO findTVShowCreditsById(String id) {
        return executeGet(
                config.buildUrl(URISanitizer.pathEnsureLeadingSlash(TMDB_API_VERSION + "/tv/" + id + "/credits")),
                TMDbCreditsResponseDTO.class,
                headers -> headers.set("Authorization", config.getAuthHeader())
        );
    }

    @Nullable
    public TMDbFindItemResponseDTO findMovieByIMDbId(String imdbId) {
        TMDbFindResponseDTO response = findByIMDbId(imdbId);
        return ObjectUtils.isEmpty(response.movieResults()) ? null : response.movieResults().getFirst();
    }

    @Nullable
    public TMDbFindItemResponseDTO findTVShowByIMDbId(String imdbId) {
        TMDbFindResponseDTO response = findByIMDbId(imdbId);
        return ObjectUtils.isEmpty(response.tvResults()) ? null : response.tvResults().getFirst();
    }

    public TMDbSearchResponseDTO searchMoviesByName(String query, @Nullable Integer page) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(
                config.buildUrl(URISanitizer.pathEnsureLeadingSlash(TMDB_API_VERSION + "/search/movie"))
        );
        builder = builder.queryParam("query", query);
        if (page != null) {
            builder = builder.queryParam("page", page.toString());
        }

        return executeGet(
                builder.build().toUri(),
                TMDbSearchResponseDTO.class,
                headers -> headers.set("Authorization", config.getAuthHeader())
        );
    }

    public TMDbSearchResponseDTO searchSeriesByName(String query, @Nullable Integer page) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(
                config.buildUrl(URISanitizer.pathEnsureLeadingSlash(TMDB_API_VERSION + "/search/tv"))
        );
        builder = builder.queryParam("query", query);
        if (page != null) {
            builder = builder.queryParam("page", page.toString());
        }

        return executeGet(
                builder.build().toUri(),
                TMDbSearchResponseDTO.class,
                headers -> headers.set("Authorization", config.getAuthHeader())
        );
    }
}
