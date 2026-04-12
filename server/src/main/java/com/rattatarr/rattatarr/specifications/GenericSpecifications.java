package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.entities.BaseEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class GenericSpecifications {

    public static <T extends BaseEntity> Specification<T> createdAtAfter(Instant date) {
        return (root, query, cb) -> date == null ? null :
                cb.greaterThan(root.get("createdAt"), date);
    }

    public static <T extends BaseEntity> Specification<T> createdAtBefore(Instant date) {
        return (root, query, cb) -> date == null ? null :
                cb.lessThan(root.get("createdAt"), date);
    }

    public static <T extends BaseEntity> Specification<T> updatedAtAfter(Instant date) {
        return (root, query, cb) -> date == null ? null :
                cb.greaterThan(root.get("updatedAt"), date);
    }

    public static <T extends BaseEntity> Specification<T> updatedAtBefore(Instant date) {
        return (root, query, cb) -> date == null ? null :
                cb.lessThan(root.get("updatedAt"), date);
    }

    public static <T extends BaseEntity> Specification<T> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }
}
