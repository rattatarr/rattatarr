package com.rattatarr.rattatarr.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public final class RadarrClientExceptions extends BaseRattatarrExceptions {

    private final HttpStatusCode responseStatusCode;
    private final String responseBody;

    public RadarrClientExceptions(String message, HttpStatus status, HttpStatusCode responseStatusCode, String responseBody, Throwable cause) {
        super(message, cause, status);
        this.responseStatusCode = responseStatusCode;
        this.responseBody = responseBody;
    }

    public HttpStatusCode getResponseStatusCode() {
        return responseStatusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (responseStatusCode != null) {
            sb.append(" [Radarr Status: ").append(responseStatusCode).append("]");
        }
        if (responseBody != null && !responseBody.isEmpty()) {
            sb.append(" [Response: ").append(responseBody).append("]");
        }
        return sb.toString();
    }
}
