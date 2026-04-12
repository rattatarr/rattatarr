package com.rattatarr.rattatarr.configs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
}
