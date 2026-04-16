package com.rattatarr.rattatarr.utils;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;

/**
 * Utility for retrying operations that can fail due to SQLite database locking.
 *
 * <p>SQLite allows only one writer at a time. Under concurrent request load (e.g. two
 * profiles chatting simultaneously) a write may encounter {@code SQLITE_BUSY}. This
 * helper retries such operations with a linear back-off before giving up.
 *
 * <p>Example usage:
 * <pre>{@code
 * SQLiteRetry.execute(
 *     () -> repository.save(entity),
 *     logger,
 *     "save conversation message"
 * );
 * }</pre>
 */
@NullMarked
public final class SQLiteRetry {

    private static final int MAX_RETRIES = 3;
    private static final long BACKOFF_MS = 200;

    private SQLiteRetry() {
    }

    /**
     * Executes {@code operation}, retrying up to {@value MAX_RETRIES} times when
     * SQLite signals a busy/locked database. Each retry waits an additional
     * {@value BACKOFF_MS} ms (200 ms, 400 ms, 600 ms).
     *
     * @param operation the write operation to execute
     * @param logger    caller's logger for retry warnings
     * @param context   short description used in log messages (e.g. "save conversation message")
     * @throws RuntimeException the original exception if retries are exhausted or the
     *                          failure is not a lock error
     */
    @SuppressWarnings("BusyWait")
    public static void execute(Runnable operation, Logger logger, String context) {
        int attempts = 0;
        while (true) {
            try {
                operation.run();
                return;
            } catch (Exception e) {
                if (attempts < MAX_RETRIES && isLocked(e)) {
                    attempts++;
                    logger.warn("SQLite database locked during '{}', retry {}/{}", context, attempts, MAX_RETRIES);
                    try {
                        Thread.sleep(BACKOFF_MS * attempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                } else {
                    throw e;
                }
            }
        }
    }

    public static boolean isLocked(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            String msg = cause.getMessage();
            if (msg != null && (msg.contains("SQLITE_BUSY") || msg.contains("database is locked"))) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
