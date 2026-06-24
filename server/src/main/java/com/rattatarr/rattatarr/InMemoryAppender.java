package com.rattatarr.rattatarr;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import com.rattatarr.rattatarr.models.LogEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryAppender extends AppenderBase<ILoggingEvent> {
    private volatile List<LogEvent> logEvents = new CopyOnWriteArrayList<>();
    private final AtomicLong sequence = new AtomicLong(0);
    private volatile boolean trimWarned = false;

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${rattatarr.logging.max-events:10000}")
    private int maxEvents = 10_000;

    @Override
    protected void append(ILoggingEvent event) {
        long seq = sequence.getAndIncrement();
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        String stackTrace = throwableProxy != null ? ThrowableProxyUtil.asString(throwableProxy) : null;
        LogEvent logEvent = new LogEvent(
                event.getTimeStamp(),
                event.getLevel().toString(),
                event.getLoggerName(),
                event.getFormattedMessage(),
                event.getMDCPropertyMap(),
                serviceName,
                event.getThreadName(),
                seq,
                stackTrace
        );
        boolean shouldWarn = false;
        synchronized (this) {
            if (logEvents.size() >= maxEvents) {
                if (!trimWarned) {
                    trimWarned = true;
                    shouldWarn = true;
                }
                int keepFrom = maxEvents / 10;
                logEvents = new CopyOnWriteArrayList<>(new ArrayList<>(logEvents).subList(keepFrom, logEvents.size()));
            }
            logEvents.add(logEvent);
        }
        if (shouldWarn) {
            addWarn("InMemoryAppender at capacity (" + maxEvents + " events); oldest 10% trimmed");
        }
    }

    public List<LogEvent> getEvents() {
        return logEvents;
    }

    @PostConstruct
    public void init() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(this);
        this.start();
    }
}
