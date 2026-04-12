package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.BaseRattatarrExceptions;
import com.rattatarr.rattatarr.models.entities.BaseEntity;
import com.rattatarr.rattatarr.repositories.BaseRepository;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public abstract class BaseService<T extends BaseEntity, R extends BaseRepository<T>> {

    protected final Logger logger;
    protected final R repository;
    private final String entityName;

    protected BaseService(R repository) {
        this.logger = LoggerFactory.getLogger(getClass());
        this.repository = repository;
        this.entityName = extractEntityName();
    }

    /**
     * Override this method to specify how to update entity fields
     */
    protected abstract void updateEntity(T existing, T updated);

    /**
     * Override to validate entity before saving (create operation)
     * Throw custom exception (e.g., CommonExceptions.InvalidRequestExceptions) if validation fails
     */
    protected void validateBeforeSave(T entity) {
        // Default: no validation
    }

    /**
     * Override to validate entity before updating
     * Throw custom exception (e.g., CommonExceptions.InvalidRequestExceptions) if validation fails
     */
    protected void validateBeforeUpdate(T existing, T updated) {
        // Default: no validation
    }

    @Transactional(readOnly = true)
    public List<T> findAll() {
        logger.debug("Finding all {}", entityName);
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<T> findById(UUID id) {
        logger.debug("Finding {} with id: {}", entityName, id);
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public T findByIdOrThrow(UUID id, Function<UUID, BaseRattatarrExceptions> exceptionSupplier) {
        logger.debug("Finding {} with id: {}", entityName, id);
        return repository.findById(id).orElseThrow(() -> exceptionSupplier.apply(id));
    }


    @Transactional
    public T save(T entity) {
        validateBeforeSave(entity);
        logger.info("Saving new {}", entityName);
        return repository.save(entity);
    }

    @Transactional
    public Optional<T> update(UUID id, T updatedEntity) {
        logger.info("Updating {} with id: {}", entityName, id);
        return repository.findById(id)
                .map(existing -> {
                    validateBeforeUpdate(existing, updatedEntity);
                    updateEntity(existing, updatedEntity);
                    return repository.save(existing);
                });
    }

    @Transactional
    public boolean delete(UUID id) {
        logger.info("Soft deleting {} with id: {}", entityName, id);
        return repository.findById(id)
                .map(entity -> {
                    entity.softDelete();
                    repository.save(entity);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public List<T> saveBatch(List<T> entities) {
        if (entities.isEmpty()) {
            return List.of();
        }
        logger.info("Batch saving {} {}", entities.size(), entityName);
        entities.forEach(this::validateBeforeSave);
        return repository.saveAll(entities);
    }

    @Transactional
    public T upsert(@Nullable T existing, T newEntity) {
        if (ObjectUtils.isEmpty(existing)) {
            logger.debug("Saving new {}", entityName);
            validateBeforeSave(newEntity);
            return repository.save(newEntity);
        }

        logger.debug("Updating existing {}", entityName);
        validateBeforeUpdate(existing, newEntity);
        updateEntity(existing, newEntity);
        return repository.save(existing);
    }

    @Transactional
    public List<T> upsertBatch(List<T> entities, Function<T, T> finder) {
        if (entities.isEmpty()) {
            return List.of();
        }
        logger.info("Batch upserting {} {}", entities.size(), entityName);
        return entities.stream()
                .map(entity -> upsert(finder.apply(entity), entity))
                .toList();
    }

    private String extractEntityName() {
        try {
            ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
            Class<?> entityClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            return entityClass.getSimpleName();
        } catch (Exception e) {
            logger.warn("Could not extract entity name, using default", e);
            return "Entity";
        }
    }
}
