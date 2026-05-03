package com.rattatarr.rattatarr.clients.sonarr;

import com.rattatarr.rattatarr.clients.BaseClient;
import com.rattatarr.rattatarr.clients.Warmable;
import com.rattatarr.rattatarr.clients.sonarr.responses.SonarrSeriesResponseDTO;
import com.rattatarr.rattatarr.configs.RestClientProperties;
import com.rattatarr.rattatarr.exceptions.SonarrClientExceptions;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.List;

@NullMarked
public class SonarrClient extends BaseClient<SonarrClientExceptions> implements Warmable {
    private final SonarrConfig config;

    public SonarrClient(RestClient restClient, RestClientProperties properties, SonarrConfig config) {
        super(restClient, properties);
        this.config = config;
    }

    @Override
    protected SonarrClientExceptions mapException(RestClientResponseException e) {
        return new SonarrClientExceptions(
                "Sonarr API error: " + e.getMessage(),
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
            getMonitoredInternalSeries();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<SonarrSeriesResponseDTO> getMonitoredInternalSeries() {
        URI uri = URI.create(config.buildUrl("/series"));
        return executeGet(
                uri,
                new ParameterizedTypeReference<List<SonarrSeriesResponseDTO>>() {
                },
                headers -> headers.set("X-Api-Key", config.getAuthHeader())
        );
    }
}
