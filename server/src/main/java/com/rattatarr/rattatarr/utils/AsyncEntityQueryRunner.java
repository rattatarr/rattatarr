package com.rattatarr.rattatarr.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

public final class AsyncEntityQueryRunner {

    private final EntityManagerFactory emf;
    private final Executor executor;
    private final Logger logger;

    public AsyncEntityQueryRunner(EntityManagerFactory emf, Executor executor, Logger logger) {
        this.emf = emf;
        this.executor = executor;
        this.logger = logger;
    }

    public <T> CompletableFuture<T> query(String label, Function<EntityManager, T> fn) {
        return CompletableFuture.supplyAsync(() -> {
            long start = System.nanoTime();
            try (EntityManager em = emf.createEntityManager()) {
                T result = fn.apply(em);
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                logger.debug("Query '{}' completed in {} ms", label, elapsedMs);
                return result;
            } catch (RuntimeException e) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                logger.debug("Query '{}' failed after {} ms", label, elapsedMs);
                throw e;
            }
        }, executor);
    }
}
