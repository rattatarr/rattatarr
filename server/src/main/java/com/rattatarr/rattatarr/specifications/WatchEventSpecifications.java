package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.WatchEventType;
import com.rattatarr.rattatarr.models.entities.WatchEvent;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

@NullMarked
public final class WatchEventSpecifications {

    private WatchEventSpecifications() {
    }

    public static Specification<WatchEvent> forProfile(@Nullable UUID profileId) {
        return (root, query, cb) -> {
            if (profileId == null) return cb.disjunction();
            return cb.equal(root.get("profile").get("id"), profileId);
        };
    }

    public static Specification<WatchEvent> afterOrAt(@Nullable Instant start) {
        return (root, query, cb) -> {
            if (start == null) return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("watchedAt"), start);
        };
    }

    public static Specification<WatchEvent> before(@Nullable Instant end) {
        return (root, query, cb) -> {
            if (end == null) return cb.conjunction();
            return cb.lessThan(root.get("watchedAt"), end);
        };
    }

    public static Specification<WatchEvent> excludeEventType(WatchEventType type) {
        return (root, query, cb) -> cb.notEqual(root.get("eventType"), type);
    }

    public static Specification<WatchEvent> forMediaType(@Nullable MediaType mediaType) {
        return (root, query, cb) -> {
            if (mediaType == null) return cb.conjunction();
            return cb.equal(root.get("mediaItem").get("mediaType"), mediaType);
        };
    }

    /**
     * Keeps only the best event per (profile, mediaItem, episode, UTC day).
     * COMPLETE beats PROGRESS; within the same type the latest watchedAt wins.
     * Implemented as NOT EXISTS correlated subquery so the dedup happens in SQL.
     */
    public static Specification<WatchEvent> deduplicated() {
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<WatchEvent> w2 = sub.from(WatchEvent.class);

            // same session key: profile + mediaItem + episode (null-safe) + UTC day
            Predicate sameProfile = cb.equal(w2.get("profile").get("id"), root.get("profile").get("id"));
            Predicate sameMedia = cb.equal(w2.get("mediaItem").get("id"), root.get("mediaItem").get("id"));

            Predicate sameEpisode = cb.or(
                    cb.and(cb.isNull(root.get("episode")), cb.isNull(w2.get("episode"))),
                    cb.and(
                            cb.isNotNull(root.get("episode")),
                            cb.isNotNull(w2.get("episode")),
                            cb.equal(w2.get("episode").get("id"), root.get("episode").get("id"))
                    )
            );

            // UTC day = epoch_millis / 86_400_000  (integer division truncates = floor for positive values)
            Expression<Long> rootDay = cb.quot(root.get("watchedAt").as(Long.class), 86_400_000L).as(Long.class);
            Expression<Long> subDay = cb.quot(w2.get("watchedAt").as(Long.class), 86_400_000L).as(Long.class);
            Predicate sameDay = cb.equal(subDay, rootDay);

            // w2 is strictly better than root:
            //   - COMPLETE beats non-COMPLETE, or
            //   - same type but newer watchedAt
            Predicate w2Complete = cb.equal(w2.get("eventType"), WatchEventType.COMPLETE);
            Predicate rootNotComplete = cb.notEqual(root.get("eventType"), WatchEventType.COMPLETE);
            Predicate w2SameTypeNewer = cb.and(
                    cb.equal(w2.get("eventType"), root.get("eventType")),
                    cb.greaterThan(w2.get("watchedAt").as(Instant.class), root.get("watchedAt").as(Instant.class))
            );

            sub.select(cb.literal(1L));
            sub.where(sameProfile, sameMedia, sameEpisode, sameDay,
                    cb.or(cb.and(w2Complete, rootNotComplete), w2SameTypeNewer));

            return cb.not(cb.exists(sub));
        };
    }
}
