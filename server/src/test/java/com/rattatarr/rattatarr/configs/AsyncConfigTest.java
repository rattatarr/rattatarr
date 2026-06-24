package com.rattatarr.rattatarr.configs;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class AsyncConfigTest {

    private AsyncConfig asyncConfig;

    @BeforeEach
    void setUp() {
        asyncConfig = new AsyncConfig();
    }

    @Test
    void backgroundTaskExecutor_shouldReturnConfiguredExecutor() {
        // When
        Executor executor = asyncConfig.backgroundTaskExecutor();

        // Then
        assertNotNull(executor);
        assertInstanceOf(ThreadPoolTaskExecutor.class, executor);
    }

    @Test
    void backgroundTaskExecutor_shouldHaveCorrectCorePoolSize() {
        // When
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.backgroundTaskExecutor();

        // Then
        assertEquals(2, executor.getCorePoolSize());
    }

    @Test
    void backgroundTaskExecutor_shouldHaveCorrectMaxPoolSize() {
        // When
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.backgroundTaskExecutor();

        // Then
        assertEquals(4, executor.getMaxPoolSize());
    }

    @Test
    void backgroundTaskExecutor_shouldHaveCorrectQueueCapacity() {
        // When
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.backgroundTaskExecutor();

        // Then
        assertEquals(50, executor.getQueueCapacity());
    }

    @Test
    void backgroundTaskExecutor_shouldHaveCorrectThreadNamePrefix() {
        // When
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncConfig.backgroundTaskExecutor();

        // Then
        assertEquals("rattatarr-bg-", executor.getThreadNamePrefix());
    }

    @Test
    void asyncUncaughtExceptionHandler_shouldLogErrorWithStackTrace() throws Exception {
        // Given — capture what AsyncConfig's logger emits
        Logger configLogger = (Logger) LoggerFactory.getLogger(AsyncConfig.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        configLogger.addAppender(listAppender);

        AsyncUncaughtExceptionHandler handler = asyncConfig.getAsyncUncaughtExceptionHandler();
        Method method = String.class.getMethod("trim"); // any Method works as a stand-in
        RuntimeException boom = new IllegalStateException("kaboom");

        // When
        handler.handleUncaughtException(boom, method, "param1", 42);

        // Then
        configLogger.detachAppender(listAppender);
        assertEquals(1, listAppender.list.size());
        ILoggingEvent event = listAppender.list.getFirst();
        assertEquals(Level.ERROR, event.getLevel());
        assertNotNull(event.getThrowableProxy(), "throwable must be attached so the stack trace is captured");
        assertEquals("kaboom", event.getThrowableProxy().getMessage());
        assertTrue(event.getFormattedMessage().contains("trim"));
        assertTrue(event.getFormattedMessage().contains("param1"));
    }
}
