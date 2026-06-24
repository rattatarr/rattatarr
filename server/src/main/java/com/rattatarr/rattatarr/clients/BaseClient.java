package com.rattatarr.rattatarr.clients;

import com.rattatarr.rattatarr.configs.RestClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BaseClient<E extends RuntimeException> {
    protected final RestClient restClient;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final RestClientProperties properties;

    protected BaseClient(RestClient restClient, RestClientProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    /**
     * Map RestClientResponseException to client-specific exception.
     */
    protected abstract E mapException(RestClientResponseException e);

    @SuppressWarnings("BusyWait")
    private <T> T executeWithRetry(Supplier<T> operation) {
        int attempts = 0;
        while (true) {
            try {
                return operation.get();
            } catch (RestClientResponseException e) {
                if (e.getStatusCode().is5xxServerError() && attempts < properties.getMaxRetries()) {
                    attempts++;
                    logger.warn("HTTP {} on attempt {}/{}; retrying in {}ms",
                            e.getStatusCode().value(), attempts, properties.getMaxRetries(), properties.getRetryDelay());
                    try {
                        Thread.sleep(properties.getRetryDelay());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw mapException(e);
                    }
                } else {
                    logger.warn("HTTP {} after {} attempt(s); not retrying",
                            e.getStatusCode().value(), attempts + 1);
                    throw mapException(e);
                }
            }
        }
    }

    protected <T> T executeGet(String uri, Class<T> responseType) {
        return executeWithRetry(() -> restClient.get()
                .uri(uri)
                .retrieve()
                .body(responseType));
    }

    protected <T> T executeGet(String uri, Class<T> responseType, Consumer<HttpHeaders> headersConsumer) {
        return executeWithRetry(() -> restClient.get()
                .uri(uri)
                .headers(headersConsumer)
                .retrieve()
                .body(responseType));
    }

    protected <T> T executeGet(String uri, ParameterizedTypeReference<T> responseType, Consumer<HttpHeaders> headersConsumer) {
        return executeWithRetry(() -> restClient.get()
                .uri(uri)
                .headers(headersConsumer)
                .retrieve()
                .body(responseType));
    }

    protected <T> T executeGet(
            URI uri,
            Class<T> responseType,
            Consumer<HttpHeaders> headersConsumer
    ) {
        return executeWithRetry(() -> restClient.get()
                .uri(uri)
                .headers(headersConsumer)
                .retrieve()
                .body(responseType));
    }

    protected <T> T executeGet(
            URI uri,
            ParameterizedTypeReference<T> responseType,
            Consumer<HttpHeaders> headersConsumer
    ) {
        return executeWithRetry(() -> restClient.get()
                .uri(uri)
                .headers(headersConsumer)
                .retrieve()
                .body(responseType));
    }

    protected <T, R> R executePost(String uri, T body, Class<R> responseType) {
        return executeWithRetry(() -> restClient.post()
                .uri(uri)
                .body(body)
                .retrieve()
                .body(responseType));
    }

    protected <T, R> R executePost(String uri, T body, Class<R> responseType, Consumer<HttpHeaders> headersConsumer) {
        return executeWithRetry(() -> restClient.post()
                .uri(uri)
                .headers(headersConsumer)
                .body(body)
                .retrieve()
                .body(responseType));
    }

    protected <T, R> R executePut(String uri, T body, Class<R> responseType) {
        return executeWithRetry(() -> restClient.put()
                .uri(uri)
                .body(body)
                .retrieve()
                .body(responseType));
    }

    protected <T, R> R executePut(String uri, T body, Class<R> responseType, Consumer<HttpHeaders> headersConsumer) {
        return executeWithRetry(() -> restClient.put()
                .uri(uri)
                .headers(headersConsumer)
                .body(body)
                .retrieve()
                .body(responseType));
    }

    protected void executeDelete(String uri) {
        executeWithRetry(() -> {
            restClient.delete()
                    .uri(uri)
                    .retrieve()
                    .toBodilessEntity();
            return null;
        });
    }

    protected void executeDelete(String uri, Consumer<HttpHeaders> headersConsumer) {
        executeWithRetry(() -> {
            restClient.delete()
                    .uri(uri)
                    .headers(headersConsumer)
                    .retrieve()
                    .toBodilessEntity();
            return null;
        });
    }
}
