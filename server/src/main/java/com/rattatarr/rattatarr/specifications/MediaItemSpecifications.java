package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemCast;
import com.rattatarr.rattatarr.models.entities.MediaItemCrew;
import com.rattatarr.rattatarr.models.entities.MediaItemRating;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Subquery;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@NullMarked
public class MediaItemSpecifications {
    public static Specification<MediaItem> isMovie() {
        return (root, query, cb) -> cb.equal(root.get("mediaType"), "MOVIE");
    }

    public static Specification<MediaItem> isSeries() {
        return (root, query, cb) -> cb.equal(root.get("mediaType"), "SERIES");
    }

    public static Specification<MediaItem> hasId(@Nullable UUID id) {
        return (root, query, cb) -> id == null ? null :
                cb.equal(root.get("id"), id);

    }

    public static Specification<MediaItem> titleLike(@Nullable String title) {
        return (root, query, cb) -> title == null ? null :
                cb.like(cb.lower(root.get("title")),
                        "%" + title.toLowerCase() + "%");
    }

    public static Specification<MediaItem> releasedAfter(@Nullable Integer year) {
        return (root, query, cb) -> year == null ? null :
                cb.greaterThanOrEqualTo(root.get("productionYear"), year);
    }

    public static Specification<MediaItem> releasedBefore(@Nullable Integer year) {
        return (root, query, cb) -> year == null ? null :
                cb.lessThanOrEqualTo(root.get("productionYear"), year);
    }

    public static Specification<MediaItem> genres(@Nullable Set<String> genres, boolean fetch) {
        return (root, query, cb) -> {
            Join<Object, Object> join = null;

            if (!ObjectUtils.isEmpty(genres)) {
                join = root.join("genres", JoinType.INNER);
            }

            if (fetch && MediaItem.class.equals(query.getResultType())) {
                root.fetch("genres", JoinType.LEFT);
            }

            if (join != null) {
                query.distinct(true);
                return join.get("name").in(genres);
            }

            return cb.conjunction();
        };
    }

    public static Specification<MediaItem> missingCastOrCrew() {
        return (root, query, cb) -> {
            query.distinct(true);

            // only items with a TMDb id
            var hasTmdbId = cb.and(
                    cb.isNotNull(root.get("TMDbId")),
                    cb.notEqual(root.get("TMDbId"), "")
            );

            // EXISTS cast
            Subquery<Integer> castExistsSubquery = query.subquery(Integer.class);
            var castRoot = castExistsSubquery.from(MediaItemCast.class);
            castExistsSubquery.select(cb.literal(1));
            castExistsSubquery.where(cb.equal(castRoot.get("mediaItem"), root));

            // EXISTS crew
            Subquery<Integer> crewExistsSubquery = query.subquery(Integer.class);
            var crewRoot = crewExistsSubquery.from(MediaItemCrew.class);
            crewExistsSubquery.select(cb.literal(1));
            crewExistsSubquery.where(cb.equal(crewRoot.get("mediaItem"), root));

            var missingCast = cb.not(cb.exists(castExistsSubquery));
            var missingCrew = cb.not(cb.exists(crewExistsSubquery));

            return cb.and(hasTmdbId, cb.or(missingCast, missingCrew));
        };
    }

    public static Specification<MediaItem> withMetadata() {
        return (root, query, cb) -> {
            if (MediaItem.class.equals(query.getResultType())) {
                root.fetch("metadata", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }

    public static Specification<MediaItem> staleSeries(@Nullable Instant threshold) {
        return (root, query, cb) -> {
            if (threshold == null) {
                return null;
            }

            return cb.and(
                    cb.equal(root.get("mediaType"), "SERIES"),
                    cb.lessThan(root.get("updatedAt"), threshold),
                    cb.isNull(root.get("deletedAt"))
            );
        };
    }

    public static Specification<MediaItem> ratingInRange(@Nullable UUID profileId, @Nullable Float min, @Nullable Float max) {
        return (root, query, cb) -> {
            if (profileId == null || (min == null && max == null)) {
                return null;
            }

            // Use EXISTS subquery instead of JOIN to avoid Hibernate auto-loading ratings collection.
            // We load ratings separately in MediaItemRatingsService.batchAttachRatingsForProfile().
            Subquery<Long> subquery = query.subquery(Long.class);
            var ratingRoot = subquery.from(MediaItemRating.class);
            subquery.select(cb.literal(1L));

            var mediaItemMatch = cb.equal(ratingRoot.get("mediaItem").get("id"), root.get("id"));
            var profileMatch = cb.equal(ratingRoot.get("profile").get("id"), profileId);

            if (min != null && max != null) {
                subquery.where(cb.and(mediaItemMatch, profileMatch, cb.between(ratingRoot.get("rating"), min, max)));
            } else if (min != null) {
                subquery.where(cb.and(mediaItemMatch, profileMatch, cb.greaterThanOrEqualTo(ratingRoot.get("rating"), min)));
            } else {
                subquery.where(cb.and(mediaItemMatch, profileMatch, cb.lessThanOrEqualTo(ratingRoot.get("rating"), max)));
            }

            return cb.exists(subquery);
        };
    }

    /**
     * Because ratings live in a separate table keyed by profileId, Spring Data cannot resolve
     * the "ratings" path on MediaItem as a simple column sort. This method:
     * - Strips the ratings sort from the Pageable
     * - If profileId is present, builds a spec that LEFT JOINs media_item_ratings and sets
     * ORDER BY directly on the criteria query (with an unsorted Pageable so Spring Data
     * does not overwrite it)
     * - If profileId is absent, the ratings sort is silently dropped
     */
    public static CustomSortResult resolveRatingSort(Pageable pageable, @Nullable UUID profileId) {
        Sort.Order ratingOrder = pageable.getSort().stream()
                .filter(o -> o.getProperty().equalsIgnoreCase("ratings") || o.getProperty().equalsIgnoreCase("rating"))
                .findFirst()
                .orElse(null);

        if (ratingOrder == null) {
            return new CustomSortResult((root, query, cb) -> null, pageable);
        }

        List<Sort.Order> otherOrders = pageable.getSort().stream()
                .filter(o -> !o.getProperty().equalsIgnoreCase("ratings") && !o.getProperty().equalsIgnoreCase("rating"))
                .toList();

        if (profileId == null) {
            return new CustomSortResult(
                    (root, query, cb) -> null,
                    PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(otherOrders))
            );
        }

        Specification<MediaItem> spec = (root, query, cb) -> {
            if (!MediaItem.class.equals(query.getResultType())) {
                return null;
            }

            Join<MediaItem, MediaItemRating> ratingJoin = root.join("ratings", JoinType.LEFT);
            ratingJoin.on(cb.equal(ratingJoin.get("profile").get("id"), profileId));

            List<Order> orders = new ArrayList<>();
            orders.add(ratingOrder.isAscending()
                    ? cb.asc(ratingJoin.<Float>get("rating"))
                    : cb.desc(ratingJoin.<Float>get("rating")));

            for (Sort.Order o : otherOrders) {
                orders.add(o.isAscending()
                        ? cb.asc(root.get(o.getProperty()))
                        : cb.desc(root.get(o.getProperty())));
            }

            query.orderBy(orders);
            return null;
        };

        // Unsorted pageable: prevents Spring Data from calling query.orderBy() again
        // and overwriting the ORDER BY set above.
        return new CustomSortResult(spec, PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.unsorted()));
    }

    public static Specification<MediaItem> hasCastOrCrewMember(@Nullable UUID personId) {
        return (root, query, cb) -> {
            if (personId == null) {
                return null;
            }

            Subquery<Integer> castSubquery = query.subquery(Integer.class);
            var castRoot = castSubquery.from(MediaItemCast.class);
            castSubquery.select(cb.literal(1));
            castSubquery.where(cb.and(
                    cb.equal(castRoot.get("mediaItem"), root),
                    cb.equal(castRoot.get("person").get("id"), personId)
            ));

            Subquery<Integer> crewSubquery = query.subquery(Integer.class);
            var crewRoot = crewSubquery.from(MediaItemCrew.class);
            crewSubquery.select(cb.literal(1));
            crewSubquery.where(cb.and(
                    cb.equal(crewRoot.get("mediaItem"), root),
                    cb.equal(crewRoot.get("person").get("id"), personId)
            ));

            return cb.or(cb.exists(castSubquery), cb.exists(crewSubquery));
        };
    }

    public static Specification<MediaItem> unrated(@Nullable UUID profileId, @Nullable Boolean unrated) {
        return (root, query, cb) -> {
            if (profileId == null || !Boolean.TRUE.equals(unrated)) {
                return null;
            }

            Subquery<Long> subquery = query.subquery(Long.class);
            var ratingRoot = subquery.from(MediaItemRating.class);
            subquery.select(cb.literal(1L));
            subquery.where(cb.and(
                    cb.equal(ratingRoot.get("mediaItem").get("id"), root.get("id")),
                    cb.equal(ratingRoot.get("profile").get("id"), profileId)
            ));

            return cb.not(cb.exists(subquery));
        };
    }

    public record CustomSortResult(Specification<MediaItem> spec, Pageable pageable) {
    }
}
