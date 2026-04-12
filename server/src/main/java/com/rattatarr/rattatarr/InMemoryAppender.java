package com.rattatarr.rattatarr;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.rattatarr.rattatarr.models.LogEvent;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class InMemoryAppender extends AppenderBase<ILoggingEvent> {
    private final List<LogEvent> logEvents = new CopyOnWriteArrayList<>();

    @Value("${spring.application.name}")
    private String serviceName;

    @Override
    protected void append(ILoggingEvent event) {
        LogEvent logEvent = new LogEvent(
                event.getTimeStamp(),
                event.getLevel().toString(),
                event.getLoggerName(),
                event.getFormattedMessage(),
                event.getMDCPropertyMap(),
                serviceName
        );
        logEvents.add(logEvent);
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
