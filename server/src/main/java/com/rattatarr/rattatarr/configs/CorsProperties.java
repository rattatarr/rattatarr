package com.rattatarr.rattatarr.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@ConfigurationProperties(prefix = "rattatarr.cors")
public class CorsProperties {
    private List<String> allowedOrigins = List.of("http://localhost:3000");

    private List<String> allowedMethods =
            List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");

    private List<String> allowedHeaders = List.of("Content-Type", "Authorization", "X-Requested-With");

    private boolean allowCredentials = true;

    /**
     * Recommended: 3600 (1h) for dev, 86400 (24h) for prod.
     */
    private long maxAge = 3600;


    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }
}
