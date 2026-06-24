package com.rattatarr.rattatarr.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Tags every HTTP request with a short {@code requestId} in the MDC so all log lines
 * emitted while handling a request are correlated (and rendered as {@code [requestId=...]}
 * by the logback pattern). Honours an inbound {@code X-Request-Id} header when present so
 * correlation can span the reverse proxy / client, and echoes the id back on the response.
 *
 * <p>The id is propagated onto async worker threads by the MDC TaskDecorator configured in
 * {@link AsyncConfig}, so request-triggered background work stays correlated too.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@NullMarked
public class MdcLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID = "requestId";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (!StringUtils.hasText(requestId)) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        MDC.put(REQUEST_ID, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(REQUEST_ID);
        }
    }
}
