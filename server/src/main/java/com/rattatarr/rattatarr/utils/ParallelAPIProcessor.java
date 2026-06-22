package com.rattatarr.rattatarr.utils;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility for processing items with parallel API calls and sequential DB writes.
 *
 * <p>Use this for bulk operations that:
 * <ul>
 *   <li>Fetch data from external APIs (parallel, I/O-bound)</li>
 *   <li>Write results to SQLite database (sequential, single writer)</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * ParallelAPIProcessor.processInParallel(
 *     mediaItems,
 *     this::fetchFromTMDb,      // Fetch function (parallel)
 *     this::saveToDB,            // Save function (sequential)
 *     tmdbApiExecutor,           // Executor for parallel work
 *     logger,                    // Logger for errors
 *     "media items"              // Description for logs
 * );
 * }</pre>
 */
@NullMarked
public class ParallelAPIProcessor {

    private ParallelAPIProcessor() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Process items with parallel API fetches and sequential DB writes.
     *
     * @param <T>           Type of input items
     * @param <R>           Type of fetched results
     * @param items         List of items to process
     * @param fetchFunction Function to fetch data (runs in parallel on executor, may return null)
     * @param saveFunction  Function to save data (runs sequentially with synchronization)
     * @param executor      Thread pool for parallel API calls
     * @param logger        Logger for error messages
     * @param description   Human-readable description for log messages (e.g., "media items", "ratings")
     */
    public static <T, R> void processInParallel(
            List<T> items,
            Function<T, @Nullable R> fetchFunction,
            Consumer<R> saveFunction,
            Executor executor,
            Logger logger,
            String description
    ) {
        if (items.isEmpty()) {
            logger.info("No {} to process", description);
            return;
        }

        int total = items.size();
        logger.info("Processing {} {}...", total, description);

        Object dbWriteLock = new Object();
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger fetchFailed = new AtomicInteger();
        AtomicInteger saveFailed = new AtomicInteger();
        AtomicInteger skipped = new AtomicInteger();

        List<CompletableFuture<Void>> futures = items.stream()
                .map(item -> CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                return fetchFunction.apply(item);
                            } catch (Exception e) {
                                fetchFailed.incrementAndGet();
                                logger.error("Failed fetching {} for item: {}", description, item, e);
                                return null;
                            }
                        }, executor)
                        .thenAccept(result -> {
                            if (result == null) {
                                skipped.incrementAndGet();
                                return;
                            }
                            synchronized (dbWriteLock) {
                                try {
                                    saveFunction.accept(result);
                                    succeeded.incrementAndGet();
                                } catch (Exception e) {
                                    saveFailed.incrementAndGet();
                                    logger.error("Failed saving {} for result: {}", description, result, e);
                                }
                            }
                        })
                )
                .toList();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        int fetchErrors = fetchFailed.get();
        int saveErrors = saveFailed.get();
        int skippedCount = skipped.get() - fetchErrors;
        if (fetchErrors > 0 || saveErrors > 0) {
            logger.warn(
                    "Finished processing {} {}: {} succeeded, {} fetch errors, {} save errors, {} skipped (no data)",
                    total, description, succeeded.get(), fetchErrors, saveErrors, Math.max(skippedCount, 0));
        } else {
            logger.info(
                    "Finished processing {} {}: {} succeeded, {} skipped (no data)",
                    total, description, succeeded.get(), Math.max(skippedCount, 0));
        }
    }

    /**
     * Overload without description parameter - uses generic description.
     */
    public static <T, R> void processInParallel(
            List<T> items,
            Function<T, @Nullable R> fetchFunction,
            Consumer<R> saveFunction,
            Executor executor,
            Logger logger
    ) {
        processInParallel(items, fetchFunction, saveFunction, executor, logger, "items");
    }
}
