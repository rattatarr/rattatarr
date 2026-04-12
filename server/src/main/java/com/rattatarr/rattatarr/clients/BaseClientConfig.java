package com.rattatarr.rattatarr.clients;

public abstract class BaseClientConfig {
    protected String baseUrl;
    protected String apiKey;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Build full URL by appending path to base URL.
     */
    public String buildUrl(String path) {
        return baseUrl + (path.startsWith("/") ? path : "/" + path);
    }
}
