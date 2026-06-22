package com.rattatarr.rattatarr.utils;

import org.slf4j.MDC;

import java.util.Map;

public final class MdcContext implements AutoCloseable {
    private final Map<String, String> previous;

    private MdcContext(Map<String, String> previous) {
        this.previous = previous;
    }

    public static MdcContext of(Map<String, String> context) {
        Map<String, String> prev = MDC.getCopyOfContextMap();
        context.forEach(MDC::put);
        return new MdcContext(prev);
    }

    @Override
    public void close() {
        MDC.clear();
        if (previous != null) {
            previous.forEach(MDC::put);
        }
    }
}
