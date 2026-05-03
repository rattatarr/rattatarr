package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.WatchEventType;
import com.rattatarr.rattatarr.models.entities.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * All queries are read-only and expect a transactional context to be managed by the caller.
 */
public final class StatisticsSpecifications {
    private static Expression<String> watchedUnitKey(CriteriaBuilder cb, Root<WatchEvent> root) {
        return cb.concat(
                root.get("mediaItem").get("id").as(String.class),
                cb.concat(
                        cb.literal("#"),
                        cb.coalesce(root.get("episode").get("id").as(String.class), cb.literal("-"))
                )
        );
    }

    private static Expression<String> watchedMediaItemKey(Root<WatchEvent> root) {
        return root.get("mediaItem").get("id").as(String.class);
    }

    private static Predicate completionEquivalent(CriteriaBuilder cb, Root<WatchEvent> root) {
        Predicate complete = cb.equal(root.get("eventType"), WatchEventType.COMPLETE);
        Predicate progressNearEnd = cb.and(
                cb.equal(root.get("eventType"), WatchEventType.PROGRESS),
                cb.isNotNull(root.get("positionSeconds")),
                cb.isNotNull(root.get("mediaItem").get("runtimeMinutes")),
                cb.greaterThanOrEqualTo(
                        root.get("positionSeconds").as(Integer.class),
                        cb.prod(root.get("mediaItem").get("runtimeMinutes").as(Integer.class), 54).as(Integer.class)
                )
        );
        return cb.or(complete, progressNearEnd);
    }

    private static void addRatedAtRange(CriteriaBuilder cb, List<Predicate> preds, Root<?> root, Instant from, Instant to) {
        if (from != null) preds.add(cb.greaterThanOrEqualTo(root.get("ratedAt"), from));
        if (to != null) preds.add(cb.lessThan(root.get("ratedAt"), to));
    }

    private static void addWatchedAtRange(CriteriaBuilder cb, List<Predicate> preds, Root<?> root, Instant from, Instant to) {
        if (from != null) preds.add(cb.greaterThanOrEqualTo(root.get("watchedAt"), from));
        if (to != null) preds.add(cb.lessThan(root.get("watchedAt"), to));
    }

    // -------------------------------------------------------------------------
    // Overall / aggregate
    // -------------------------------------------------------------------------

    public static Long queryTotalItems(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> q = cb.createQuery(Long.class);
        Root<MediaItem> root = q.from(MediaItem.class);
        q.select(cb.count(root));
        Long result = em.createQuery(q).getSingleResult();
        return result != null ? result : 0L;
    }

    public static List<Tuple> queryMediaTypeTotal(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItem> root = q.from(MediaItem.class);

        q.select(cb.tuple(
                root.get("mediaType").alias("mediaType"),
                cb.count(root).alias("totalCount")
        ));
        q.groupBy(root.get("mediaType"));

        return em.createQuery(q).getResultList();
    }

    public static Tuple queryOverallStats(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);

        q.select(cb.tuple(
                cb.count(root).alias("totalRatings"),
                cb.avg(root.get("rating")).alias("averageRating"),
                cb.min(root.get("rating")).alias("minRating"),
                cb.max(root.get("rating")).alias("maxRating")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        addRatedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        return em.createQuery(q).getSingleResult();
    }

    // -------------------------------------------------------------------------
    // Genres
    // -------------------------------------------------------------------------

    public static List<Tuple> queryTopGenresBy(
            EntityManager em, UUID profileId, float ratingThreshold, int limit, SortBy sortBy,
            Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);
        Join<MediaItemRating, MediaItem> mediaJoin = root.join("mediaItem");
        Join<MediaItem, Genre> genreJoin = mediaJoin.join("genres");

        q.select(cb.tuple(
                genreJoin.get("name").alias("genreName"),
                cb.count(root).alias("count"),
                cb.avg(root.get("rating")).alias("averageRating")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        if (sortBy == SortBy.SCORE) preds.add(cb.greaterThanOrEqualTo(root.get("rating"), ratingThreshold));
        addRatedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.groupBy(genreJoin.get("name"));
        if (sortBy == SortBy.SCORE) {
            q.orderBy(cb.desc(cb.avg(root.get("rating"))), cb.desc(cb.count(root)));
        } else {
            q.orderBy(cb.desc(cb.count(root)), cb.desc(cb.avg(root.get("rating"))));
        }

        return em.createQuery(q).setMaxResults(limit).getResultList();
    }

    /**
     * Genre distribution broken down by calendar year.
     *
     * <p>Returns all (year, genreName, count, averageRating) tuples for the profile, ordered
     * {@code year ASC}, {@code count DESC} within each year. The caller is responsible for
     * slicing to the top-{@code limit} genres per year, since SQLite does not support
     * per-partition LIMIT via window functions.
     *
     * <p>The year is derived from {@code ratedAt} using SQLite's
     * {@code strftime('%Y', datetime(ratedAt/1000, 'unixepoch'))} expression.
     *
     * @param limit unused at the DB level — present for API symmetry; slicing is done in the
     *              service layer via {@code limit} genres per year group.
     */
    public static List<Tuple> queryGenreOverTime(EntityManager em, UUID profileId, int limit) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);
        Join<MediaItemRating, MediaItem> mediaJoin = root.join("mediaItem");
        Join<MediaItem, Genre> genreJoin = mediaJoin.join("genres");

        Expression<String> year = cb.function(
                "strftime",
                String.class,
                cb.literal("%Y"),
                cb.function(
                        "datetime",
                        String.class,
                        cb.quot(root.get("ratedAt"), 1000L).as(String.class),
                        cb.literal("unixepoch")));

        q.select(cb.tuple(
                year.alias("year"),
                genreJoin.get("name").alias("genreName"),
                cb.count(root).alias("count"),
                cb.avg(root.get("rating")).alias("averageRating")
        ));
        q.where(
                cb.equal(root.get("profile").get("id"), profileId),
                cb.isNotNull(root.get("ratedAt"))
        );
        q.groupBy(year, genreJoin.get("name"));
        q.orderBy(cb.asc(year), cb.desc(cb.count(root)), cb.desc(cb.avg(root.get("rating"))));

        return em.createQuery(q).getResultList();
    }

    public static List<Tuple> queryJellyfinTopGenresBy(
            EntityManager em, UUID profileId, int limit, SortBy sortBy, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<WatchEvent> root = q.from(WatchEvent.class);
        Join<WatchEvent, MediaItem> mediaJoin = root.join("mediaItem");
        Join<MediaItem, Genre> genreJoin = mediaJoin.join("genres");
        Expression<String> unitKey = watchedMediaItemKey(root);

        q.select(cb.tuple(
                genreJoin.get("name").alias("genreName"),
                cb.countDistinct(unitKey).alias("count"),
                cb.literal(0.0d).alias("averageRating")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        preds.add(completionEquivalent(cb, root));
        addWatchedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.groupBy(genreJoin.get("name"));
        if (sortBy == SortBy.SCORE) {
            q.orderBy(cb.asc(genreJoin.get("name")));
        } else {
            q.orderBy(cb.desc(cb.countDistinct(unitKey)), cb.asc(genreJoin.get("name")));
        }

        return em.createQuery(q).setMaxResults(limit).getResultList();
    }

    public static List<Tuple> queryJellyfinGenreOverTime(EntityManager em, UUID profileId, int limit) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<WatchEvent> root = q.from(WatchEvent.class);
        Join<WatchEvent, MediaItem> mediaJoin = root.join("mediaItem");
        Join<MediaItem, Genre> genreJoin = mediaJoin.join("genres");
        Expression<String> unitKey = watchedMediaItemKey(root);

        Expression<Long> epochSeconds = cb.quot(
                root.get("watchedAt").as(Long.class),
                cb.literal(1000L)
        ).as(Long.class);

        Expression<String> year = cb.function(
                "strftime",
                String.class,
                cb.literal("%Y"),
                cb.function(
                        "datetime",
                        String.class,
                        epochSeconds,
                        cb.literal("unixepoch")));

        q.select(cb.tuple(
                year.alias("year"),
                genreJoin.get("name").alias("genreName"),
                cb.countDistinct(unitKey).alias("count"),
                cb.literal(0.0d).alias("averageRating")
        ));
        q.where(
                cb.equal(root.get("profile").get("id"), profileId),
                cb.isNotNull(root.get("watchedAt")),
                completionEquivalent(cb, root)
        );
        q.groupBy(year, genreJoin.get("name"));
        q.orderBy(cb.asc(year), cb.desc(cb.countDistinct(unitKey)), cb.asc(genreJoin.get("name")));

        return em.createQuery(q).getResultList();
    }

    // -------------------------------------------------------------------------
    // People (directors, producers, actors)
    // -------------------------------------------------------------------------

    public static List<Tuple> queryFavoriteCrewByJob(
            EntityManager em, UUID profileId, String job, int minCount, int limit, SortBy sortBy,
            Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);
        Join<MediaItemRating, MediaItem> mediaJoin = root.join("mediaItem");
        Join<MediaItem, MediaItemCrew> crewJoin = mediaJoin.join("crew");
        Join<MediaItemCrew, Person> personJoin = crewJoin.join("person");

        q.select(cb.tuple(
                personJoin.get("id").alias("personId"),
                personJoin.get("name").alias("name"),
                personJoin.get("profilePathUrl").alias("profilePathUrl"),
                cb.avg(root.get("rating")).alias("averageRating"),
                cb.countDistinct(mediaJoin.get("id")).alias("itemCount")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        preds.add(cb.equal(crewJoin.get("job"), job));
        addRatedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.groupBy(personJoin.get("id"), personJoin.get("name"), personJoin.get("profilePathUrl"));
        // When sorting by score, require at least 5 rated titles to avoid one-hit 10/10 outliers.
        long effectiveMinCount = sortBy == SortBy.SCORE ? Math.max(minCount, 5) : minCount;
        q.having(cb.greaterThanOrEqualTo(cb.countDistinct(mediaJoin.get("id")), effectiveMinCount));
        if (sortBy == SortBy.SCORE) {
            q.orderBy(cb.desc(cb.avg(root.get("rating"))), cb.desc(cb.countDistinct(mediaJoin.get("id"))));
        } else {
            q.orderBy(cb.desc(cb.countDistinct(mediaJoin.get("id"))), cb.desc(cb.avg(root.get("rating"))));
        }

        return em.createQuery(q).setMaxResults(limit).getResultList();
    }

    public static List<Tuple> queryFavoriteActors(
            EntityManager em, UUID profileId, int minCount, int limit, SortBy sortBy,
            Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);
        Join<MediaItemRating, MediaItem> mediaJoin = root.join("mediaItem");
        Join<MediaItem, MediaItemCast> castJoin = mediaJoin.join("cast");
        Join<MediaItemCast, Person> personJoin = castJoin.join("person");

        q.select(cb.tuple(
                personJoin.get("id").alias("personId"),
                personJoin.get("name").alias("name"),
                personJoin.get("profilePathUrl").alias("profilePathUrl"),
                cb.avg(root.get("rating")).alias("averageRating"),
                cb.countDistinct(mediaJoin.get("id")).alias("itemCount")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        addRatedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.groupBy(personJoin.get("id"), personJoin.get("name"), personJoin.get("profilePathUrl"));
        // When sorting by score, require at least 5 rated titles to avoid one-hit 10/10 outliers.
        long effectiveMinCount = sortBy == SortBy.SCORE ? Math.max(minCount, 5) : minCount;
        q.having(cb.greaterThanOrEqualTo(cb.countDistinct(mediaJoin.get("id")), effectiveMinCount));
        if (sortBy == SortBy.SCORE) {
            q.orderBy(cb.desc(cb.avg(root.get("rating"))), cb.desc(cb.countDistinct(mediaJoin.get("id"))));
        } else {
            q.orderBy(cb.desc(cb.countDistinct(mediaJoin.get("id"))), cb.desc(cb.avg(root.get("rating"))));
        }

        return em.createQuery(q).setMaxResults(limit).getResultList();
    }

    // -------------------------------------------------------------------------
    // Rating distribution and preferences
    // -------------------------------------------------------------------------

    public static List<Tuple> queryRatingDistribution(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);

        Expression<Object> ratingRange = cb.selectCase()
                .when(cb.lessThan(root.get("rating"), 2.0f), "0-2")
                .when(cb.lessThan(root.get("rating"), 4.0f), "2-4")
                .when(cb.lessThan(root.get("rating"), 6.0f), "4-6")
                .when(cb.lessThan(root.get("rating"), 8.0f), "6-8")
                .otherwise("8-10");

        q.select(cb.tuple(
                ratingRange.alias("range"),
                cb.count(root).alias("count")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        addRatedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.groupBy(ratingRange);
        q.orderBy(cb.asc(ratingRange));

        return em.createQuery(q).getResultList();
    }

    /**
     * Rating distribution bucketed by rounded integer score (1–10).
     *
     * <p>Each rating is rounded to the nearest integer via SQLite's {@code ROUND()} function, so
     * 7.5 → 8 and 6.5 → 7 (banker's rounding in SQLite). Only buckets with at least one rating
     * are returned — the service layer is responsible for filling in zero-count buckets for the
     * full 1–10 range.
     *
     * <p>Results are ordered ascending by bucket value (1, 2, … 10).
     */
    public static List<Tuple> queryRatingDistributionByInteger(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);

        Expression<Integer> bucket = cb.function("ROUND", Integer.class, root.get("rating"));

        q.select(cb.tuple(
                bucket.alias("bucket"),
                cb.count(root).alias("count")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        addRatedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.groupBy(bucket);
        q.orderBy(cb.asc(bucket));

        return em.createQuery(q).getResultList();
    }

    public static List<Tuple> queryMediaTypeBreakdown(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);
        Join<MediaItemRating, MediaItem> mediaJoin = root.join("mediaItem");

        q.select(cb.tuple(
                mediaJoin.get("mediaType").alias("mediaType"),
                cb.count(root).alias("count"),
                cb.avg(root.get("rating")).alias("averageRating")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        addRatedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.groupBy(mediaJoin.get("mediaType"));

        return em.createQuery(q).getResultList();
    }

    public static List<Tuple> queryDecadePreferences(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);
        Join<MediaItemRating, MediaItem> mediaJoin = root.join("mediaItem");

        Expression<Integer> productionYear = mediaJoin.get("productionYear");
        Expression<Number> decade = cb.prod(cb.quot(productionYear, 10), 10);

        q.select(cb.tuple(
                decade.alias("decade"),
                cb.count(root).alias("count"),
                cb.avg(root.get("rating")).alias("averageRating")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        preds.add(cb.isNotNull(mediaJoin.get("productionYear")));
        addRatedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.groupBy(decade);
        q.orderBy(cb.desc(decade));

        return em.createQuery(q).getResultList();
    }

    public static Tuple queryTrendForPeriod(EntityManager em, UUID profileId, Instant start, Instant end) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);

        q.select(cb.tuple(
                cb.count(root).alias("count"),
                cb.avg(root.get("rating")).alias("averageRating")
        ));
        q.where(
                cb.equal(root.get("profile").get("id"), profileId),
                cb.greaterThanOrEqualTo(root.get("ratedAt"), start),
                cb.lessThanOrEqualTo(root.get("ratedAt"), end)
        );

        return em.createQuery(q).getSingleResult();
    }

    public static List<Tuple> queryJellyfinMediaTypeBreakdown(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<WatchEvent> root = q.from(WatchEvent.class);
        Join<WatchEvent, MediaItem> mediaJoin = root.join("mediaItem");
        Expression<String> unitKey = watchedMediaItemKey(root);

        q.select(cb.tuple(
                mediaJoin.get("mediaType").alias("mediaType"),
                cb.countDistinct(unitKey).alias("count"),
                cb.literal(0.0d).alias("averageRating")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        preds.add(completionEquivalent(cb, root));
        addWatchedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.groupBy(mediaJoin.get("mediaType"));

        return em.createQuery(q).getResultList();
    }

    public static List<Tuple> queryJellyfinDecadePreferences(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<WatchEvent> root = q.from(WatchEvent.class);
        Expression<String> mediaItemKey = watchedMediaItemKey(root);

        Expression<Integer> productionYear = root.get("mediaItem").get("productionYear");
        Expression<Number> decade = cb.prod(cb.quot(productionYear, 10), 10);

        q.select(cb.tuple(
                decade.alias("decade"),
                cb.countDistinct(mediaItemKey).alias("count"),
                cb.literal(0.0d).alias("averageRating")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        preds.add(cb.isNotNull(root.get("mediaItem").get("productionYear")));
        preds.add(completionEquivalent(cb, root));
        addWatchedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.groupBy(decade);
        q.orderBy(cb.desc(decade));

        return em.createQuery(q).getResultList();
    }

    public static Tuple queryJellyfinTrendForPeriod(EntityManager em, UUID profileId, Instant start, Instant end) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<WatchEvent> root = q.from(WatchEvent.class);
        Expression<String> mediaItemKey = watchedMediaItemKey(root);

        q.select(cb.tuple(
                cb.countDistinct(mediaItemKey).alias("count"),
                cb.literal(0.0d).alias("averageRating")
        ));
        q.where(
                cb.equal(root.get("profile").get("id"), profileId),
                cb.greaterThanOrEqualTo(root.get("watchedAt"), start),
                cb.lessThanOrEqualTo(root.get("watchedAt"), end),
                completionEquivalent(cb, root)
        );

        return em.createQuery(q).getSingleResult();
    }

    /**
     * Rating consistency.
     * Standard deviation of all ratings for the profile, which reflects how consistent or varied their ratings are.
     * Calculated in Java after fetching all ratings
     */
    public static List<Float> queryAllRatings(EntityManager em, UUID profileId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Float> q = cb.createQuery(Float.class);
        Root<MediaItemRating> root = q.from(MediaItemRating.class);

        q.select(root.get("rating"));
        q.where(cb.equal(root.get("profile").get("id"), profileId));

        return em.createQuery(q).getResultList();
    }

    // -------------------------------------------------------------------------
    // Time-based activity
    // -------------------------------------------------------------------------

    public static List<Tuple> queryMonthlyActivity(EntityManager em, UUID profileId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);

        Expression<String> yearMonth = cb.function(
                "strftime", String.class,
                cb.literal("%Y-%m"),
                root.get("ratedAt")
        );

        q.select(cb.tuple(
                yearMonth.alias("period"),
                cb.count(root).alias("count"),
                cb.avg(root.get("rating")).alias("averageRating")
        ));
        q.where(cb.equal(root.get("profile").get("id"), profileId));
        q.groupBy(yearMonth);
        q.orderBy(cb.desc(yearMonth));

        return em.createQuery(q).setMaxResults(12).getResultList();
    }

    // Year-scoped variant: returns all months ASC (no limit), used by year rewind.
    public static List<Tuple> queryMonthlyActivityForYear(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);

        Expression<String> yearMonth = cb.function(
                "strftime", String.class,
                cb.literal("%Y-%m"),
                root.get("ratedAt")
        );

        q.select(cb.tuple(
                yearMonth.alias("period"),
                cb.count(root).alias("count"),
                cb.avg(root.get("rating")).alias("averageRating")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        addRatedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.groupBy(yearMonth);
        q.orderBy(cb.asc(yearMonth));

        return em.createQuery(q).getResultList();
    }

    /**
     * Daily rating counts for all time, suitable for a GitHub-style activity heatmap.
     *
     * <p>Each row contains a calendar date string ({@code "YYYY-MM-DD"}) and the number of ratings
     * submitted on that day. Only days with at least one rating are returned — callers are
     * responsible for grouping by year and filling any gaps for the heatmap grid.
     *
     * <p>Results are ordered ascending by date so the service can stream-group them into years
     * without sorting.
     */
    public static List<Tuple> queryRatingHeatmap(EntityManager em, UUID profileId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);

        // ratedAt is stored as epoch milliseconds; convert to seconds for datetime().
        Expression<Long> epochSeconds = cb.quot(
                root.get("ratedAt").as(Long.class),
                cb.literal(1000L)
        ).as(Long.class);

        Expression<String> dateStr = cb.function(
                "strftime", String.class,
                cb.literal("%Y-%m-%d"),
                cb.function("datetime", String.class, epochSeconds, cb.literal("unixepoch"))
        );

        q.select(cb.tuple(
                dateStr.alias("date"),
                cb.count(root).alias("count")
        ));
        q.where(cb.equal(root.get("profile").get("id"), profileId));
        q.groupBy(dateStr);
        q.orderBy(cb.asc(dateStr));

        return em.createQuery(q).getResultList();
    }

    /**
     * Daily unique media items played from watch events, suitable for a profile activity heatmap.
     *
     * <p>Each row contains a date ({@code YYYY-MM-DD}) and the number of distinct media items
     * watched that day for the given profile.
     */
    public static List<Tuple> queryUniqueMediaPlayedHeatmap(EntityManager em, UUID profileId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<WatchEvent> root = q.from(WatchEvent.class);

        Expression<Long> epochSeconds = cb.quot(
                root.get("watchedAt").as(Long.class),
                cb.literal(1000L)
        ).as(Long.class);

        Expression<String> dateStr = cb.function(
                "strftime", String.class,
                cb.literal("%Y-%m-%d"),
                cb.function("datetime", String.class, epochSeconds, cb.literal("unixepoch"))
        );

        Expression<String> seriesEpisodeKey = watchedUnitKey(cb, root);

        q.select(cb.tuple(
                dateStr.alias("date"),
                cb.countDistinct(seriesEpisodeKey).alias("count")
        ));
        q.where(
                cb.equal(root.get("profile").get("id"), profileId),
                completionEquivalent(cb, root)
        );
        q.groupBy(dateStr);
        q.orderBy(cb.asc(dateStr));

        return em.createQuery(q).getResultList();
    }

    public static List<Tuple> queryDayOfWeekActivity(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);

        Expression<Long> epochSeconds = cb.quot(
                root.get("ratedAt").as(Long.class),
                cb.literal(1000L)
        ).as(Long.class);

        Expression<String> dayOfWeek = cb.function(
                "strftime", String.class,
                cb.literal("%w"),
                cb.function("datetime", String.class, epochSeconds, cb.literal("unixepoch"))
        );

        q.select(cb.tuple(
                dayOfWeek.alias("dayOfWeek"),
                cb.count(root).alias("count")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        addRatedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.groupBy(dayOfWeek);
        q.orderBy(cb.asc(dayOfWeek));

        return em.createQuery(q).getResultList();
    }

    public static List<Tuple> queryJellyfinDayOfWeekActivity(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<WatchEvent> root = q.from(WatchEvent.class);

        Expression<Long> epochSeconds = cb.quot(
                root.get("watchedAt").as(Long.class),
                cb.literal(1000L)
        ).as(Long.class);

        Expression<String> dateStr = cb.function(
                "strftime", String.class,
                cb.literal("%Y-%m-%d"),
                cb.function("datetime", String.class, epochSeconds, cb.literal("unixepoch"))
        );
        Expression<String> dayUnitKey = cb.concat(dateStr, cb.concat(":", watchedMediaItemKey(root)));

        Expression<String> dayOfWeek = cb.function(
                "strftime", String.class,
                cb.literal("%w"),
                cb.function("datetime", String.class, epochSeconds, cb.literal("unixepoch"))
        );

        q.select(cb.tuple(
                dayOfWeek.alias("dayOfWeek"),
                cb.countDistinct(dayUnitKey).alias("count")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        preds.add(completionEquivalent(cb, root));
        addWatchedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.groupBy(dayOfWeek);
        q.orderBy(cb.asc(dayOfWeek));

        return em.createQuery(q).getResultList();
    }

    // -------------------------------------------------------------------------
    // Runtime
    // -------------------------------------------------------------------------

    /**
     * Aggregate runtime stats from watched activity without double counting rated items.
     *
     * <p>Counts unique watched units per profile:
     * <ul>
     *   <li>Movies: one unit per {@code mediaItem.id}</li>
     *   <li>Series: one unit per distinct episode ({@code episode.id})</li>
     * </ul>
     *
     * <p>Runtime source:
     * <ul>
     *   <li>Movies: {@code mediaItem.runtimeMinutes}</li>
     *   <li>Series episodes: {@code episode.runtimeMinutes} with fallback to parent
     *       {@code mediaItem.runtimeMinutes}</li>
     * </ul>
     */
    public static Tuple queryRuntimeStats(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Tuple> movieQuery = cb.createTupleQuery();
        Root<WatchEvent> movieRoot = movieQuery.from(WatchEvent.class);

        List<Predicate> moviePreds = new ArrayList<>();
        moviePreds.add(cb.equal(movieRoot.get("profile").get("id"), profileId));
        moviePreds.add(cb.equal(movieRoot.get("mediaItem").get("mediaType"), MediaType.MOVIE));
        moviePreds.add(cb.isNotNull(movieRoot.get("mediaItem").get("runtimeMinutes")));
        moviePreds.add(completionEquivalent(cb, movieRoot));
        addWatchedAtRange(cb, moviePreds, movieRoot, from, to);

        movieQuery.select(cb.tuple(
                movieRoot.get("mediaItem").get("id").alias("unitId"),
                movieRoot.get("mediaItem").get("runtimeMinutes").alias("runtime")
        ));
        movieQuery.where(moviePreds.toArray(Predicate[]::new));
        movieQuery.groupBy(movieRoot.get("mediaItem").get("id"), movieRoot.get("mediaItem").get("runtimeMinutes"));

        CriteriaQuery<Tuple> episodeQuery = cb.createTupleQuery();
        Root<WatchEvent> episodeRoot = episodeQuery.from(WatchEvent.class);
        Expression<Integer> episodeRuntime = cb.coalesce(
                episodeRoot.get("episode").get("runtimeMinutes").as(Integer.class),
                episodeRoot.get("mediaItem").get("runtimeMinutes").as(Integer.class)
        );

        List<Predicate> episodePreds = new ArrayList<>();
        episodePreds.add(cb.equal(episodeRoot.get("profile").get("id"), profileId));
        episodePreds.add(cb.equal(episodeRoot.get("mediaItem").get("mediaType"), MediaType.SERIES));
        episodePreds.add(cb.isNotNull(episodeRoot.get("episode")));
        episodePreds.add(cb.isNotNull(episodeRuntime));
        episodePreds.add(completionEquivalent(cb, episodeRoot));
        addWatchedAtRange(cb, episodePreds, episodeRoot, from, to);

        episodeQuery.select(cb.tuple(
                episodeRoot.get("episode").get("id").alias("unitId"),
                episodeRuntime.alias("runtime")
        ));
        episodeQuery.where(episodePreds.toArray(Predicate[]::new));
        episodeQuery.groupBy(episodeRoot.get("episode").get("id"), episodeRuntime);

        List<Tuple> movies = em.createQuery(movieQuery).getResultList();
        List<Tuple> episodes = em.createQuery(episodeQuery).getResultList();

        java.util.List<Integer> runtimes = new java.util.ArrayList<>();
        for (Tuple tuple : movies) {
            Integer runtime = tuple.get("runtime", Integer.class);
            if (runtime != null) runtimes.add(runtime);
        }
        for (Tuple tuple : episodes) {
            Integer runtime = tuple.get("runtime", Integer.class);
            if (runtime != null) runtimes.add(runtime);
        }

        double avg = runtimes.isEmpty()
                ? 0.0
                : runtimes.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        int longest = runtimes.stream().mapToInt(Integer::intValue).max().orElse(0);
        int shortest = runtimes.stream().mapToInt(Integer::intValue).min().orElse(0);

        CriteriaQuery<Tuple> aggregate = cb.createTupleQuery();
        aggregate.select(cb.tuple(
                cb.literal(avg).alias("averageRuntime"),
                cb.literal(longest).alias("longestRuntime"),
                cb.literal(shortest).alias("shortestRuntime")
        ));
        return em.createQuery(aggregate).getSingleResult();
    }

    public static Tuple queryRatingRuntimeStats(EntityManager em, UUID profileId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);
        Join<MediaItemRating, MediaItem> mediaJoin = root.join("mediaItem");

        q.select(cb.tuple(
                cb.avg(mediaJoin.get("runtimeMinutes")).alias("averageRuntime"),
                cb.max(mediaJoin.get("runtimeMinutes")).alias("longestRuntime"),
                cb.min(mediaJoin.get("runtimeMinutes")).alias("shortestRuntime")
        ));
        q.where(
                cb.equal(root.get("profile").get("id"), profileId),
                cb.isNotNull(mediaJoin.get("runtimeMinutes"))
        );

        return em.createQuery(q).getSingleResult();
    }

    public static Long queryMoviesTotalRuntime(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<WatchEvent> root = q.from(WatchEvent.class);

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        preds.add(cb.equal(root.get("mediaItem").get("mediaType"), MediaType.MOVIE));
        preds.add(cb.isNotNull(root.get("mediaItem").get("runtimeMinutes")));
        preds.add(completionEquivalent(cb, root));
        addWatchedAtRange(cb, preds, root, from, to);

        q.select(cb.tuple(
                root.get("mediaItem").get("id").alias("movieId"),
                root.get("mediaItem").get("runtimeMinutes").alias("runtime")
        ));
        q.where(preds.toArray(Predicate[]::new));
        q.groupBy(root.get("mediaItem").get("id"), root.get("mediaItem").get("runtimeMinutes"));

        List<Tuple> rows = em.createQuery(q).getResultList();
        long total = 0L;
        for (Tuple row : rows) {
            Integer runtime = row.get("runtime", Integer.class);
            if (runtime != null) total += runtime;
        }
        return total;
    }

    public static Long queryRatedMoviesTotalRuntime(EntityManager em, UUID profileId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> q = cb.createQuery(Long.class);
        Root<MediaItemRating> root = q.from(MediaItemRating.class);
        Join<MediaItemRating, MediaItem> mediaJoin = root.join("mediaItem");

        q.select(cb.sumAsLong(mediaJoin.get("runtimeMinutes")));
        q.where(
                cb.equal(root.get("profile").get("id"), profileId),
                cb.equal(mediaJoin.get("mediaType"), MediaType.MOVIE),
                cb.isNotNull(mediaJoin.get("runtimeMinutes"))
        );

        Long result = em.createQuery(q).getSingleResult();
        return result != null ? result : 0L;
    }

    /**
     * Total runtime from unique watched episodes for a profile. Uses episode runtime when present,
     * otherwise falls back to parent series runtime.
     */
    public static Long querySeriesRuntimeTotal(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<WatchEvent> root = q.from(WatchEvent.class);
        Expression<Integer> runtimeExpr = cb.coalesce(
                root.get("episode").get("runtimeMinutes").as(Integer.class),
                root.get("mediaItem").get("runtimeMinutes").as(Integer.class)
        );

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        preds.add(cb.equal(root.get("mediaItem").get("mediaType"), MediaType.SERIES));
        preds.add(cb.isNotNull(root.get("episode")));
        preds.add(cb.isNotNull(runtimeExpr));
        preds.add(completionEquivalent(cb, root));
        addWatchedAtRange(cb, preds, root, from, to);

        q.select(cb.tuple(
                root.get("episode").get("id").alias("episodeId"),
                runtimeExpr.alias("runtime")
        ));
        q.where(preds.toArray(Predicate[]::new));
        q.groupBy(root.get("episode").get("id"), runtimeExpr);

        List<Tuple> rows = em.createQuery(q).getResultList();
        long total = 0L;
        for (Tuple row : rows) {
            Integer runtime = row.get("runtime", Integer.class);
            if (runtime != null) total += runtime;
        }
        return total;
    }

    public static Long queryRatedSeriesRuntimeTotal(EntityManager em, UUID profileId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> ratingQuery = cb.createTupleQuery();
        Root<MediaItemRating> ratingRoot = ratingQuery.from(MediaItemRating.class);
        Join<MediaItemRating, MediaItem> ratingMediaJoin = ratingRoot.join("mediaItem");

        ratingQuery.select(cb.tuple(
                ratingMediaJoin.get("id").alias("seriesId"),
                ratingMediaJoin.get("runtimeMinutes").alias("runtimeMinutes")
        ));
        ratingQuery.where(
                cb.equal(ratingRoot.get("profile").get("id"), profileId),
                cb.equal(ratingMediaJoin.get("mediaType"), MediaType.SERIES),
                cb.isNotNull(ratingMediaJoin.get("runtimeMinutes"))
        );

        List<Tuple> ratedSeries = em.createQuery(ratingQuery).getResultList();
        if (ratedSeries.isEmpty()) {
            return 0L;
        }

        java.util.Map<UUID, Integer> runtimeBySeriesId = new java.util.HashMap<>();
        for (Tuple row : ratedSeries) {
            UUID seriesId = row.get("seriesId", UUID.class);
            Integer runtime = row.get("runtimeMinutes", Integer.class);
            if (seriesId != null && runtime != null) {
                runtimeBySeriesId.put(seriesId, runtime);
            }
        }
        if (runtimeBySeriesId.isEmpty()) {
            return 0L;
        }

        CriteriaQuery<Tuple> episodeQuery = cb.createTupleQuery();
        Root<MediaEpisode> episodeRoot = episodeQuery.from(MediaEpisode.class);
        Join<MediaEpisode, MediaSeason> seasonJoin = episodeRoot.join("mediaSeason");
        Join<MediaSeason, MediaItem> episodeMediaJoin = seasonJoin.join("mediaItem");

        episodeQuery.select(cb.tuple(
                episodeMediaJoin.get("id").alias("seriesId"),
                cb.count(episodeRoot.get("id")).alias("episodeCount")
        ));
        episodeQuery.where(episodeMediaJoin.get("id").in(runtimeBySeriesId.keySet()));
        episodeQuery.groupBy(episodeMediaJoin.get("id"));

        List<Tuple> episodeCounts = em.createQuery(episodeQuery).getResultList();

        long total = 0L;
        for (Tuple row : episodeCounts) {
            UUID seriesId = row.get("seriesId", UUID.class);
            Long episodes = row.get("episodeCount", Long.class);
            Integer runtime = runtimeBySeriesId.get(seriesId);
            if (seriesId != null && runtime != null && episodes != null) {
                total += (long) runtime * episodes;
            }
        }
        return total;
    }

    // -------------------------------------------------------------------------
    // Year Rewind — highlights
    // -------------------------------------------------------------------------

    public static List<Tuple> queryFirstRatedItem(EntityManager em, UUID profileId, Instant from, Instant to) {
        return queryHighlightItem(em, profileId, from, to, HighlightSort.FIRST_RATED);
    }

    public static List<Tuple> queryLastRatedItem(EntityManager em, UUID profileId, Instant from, Instant to) {
        return queryHighlightItem(em, profileId, from, to, HighlightSort.LAST_RATED);
    }

    public static List<Tuple> queryHighestRatedItem(EntityManager em, UUID profileId, Instant from, Instant to) {
        return queryHighlightItem(em, profileId, from, to, HighlightSort.HIGHEST_RATED);
    }

    public static List<Tuple> queryLowestRatedItem(EntityManager em, UUID profileId, Instant from, Instant to) {
        return queryHighlightItem(em, profileId, from, to, HighlightSort.LOWEST_RATED);
    }

    private static List<Tuple> queryHighlightItem(
            EntityManager em, UUID profileId, Instant from, Instant to, HighlightSort sort) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);
        Join<MediaItemRating, MediaItem> mediaJoin = root.join("mediaItem");
        Join<MediaItem, MediaItemMetadata> metadataJoin = mediaJoin.join("metadata", JoinType.LEFT);

        q.select(cb.tuple(
                mediaJoin.get("title").alias("title"),
                metadataJoin.get("posterImageUrl").alias("posterImageUrl"),
                root.get("rating").alias("rating"),
                root.get("ratedAt").alias("eventAt"),
                mediaJoin.get("mediaType").alias("mediaType")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        addRatedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.orderBy(switch (sort) {
            case FIRST_RATED -> List.of(cb.asc(root.get("ratedAt")));
            case LAST_RATED -> List.of(cb.desc(root.get("ratedAt")));
            case HIGHEST_RATED -> List.of(cb.desc(root.get("rating")), cb.desc(root.get("ratedAt")));
            case LOWEST_RATED -> List.of(cb.asc(root.get("rating")), cb.desc(root.get("ratedAt")));
        });

        return em.createQuery(q).setMaxResults(1).getResultList();
    }

    public static List<Tuple> queryFirstWatchedItem(EntityManager em, UUID profileId, Instant from, Instant to) {
        return queryWatchHighlightItem(em, profileId, from, to, false);
    }

    public static List<Tuple> queryLastWatchedItem(EntityManager em, UUID profileId, Instant from, Instant to) {
        return queryWatchHighlightItem(em, profileId, from, to, true);
    }

    private static List<Tuple> queryWatchHighlightItem(
            EntityManager em, UUID profileId, Instant from, Instant to, boolean latest) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<WatchEvent> root = q.from(WatchEvent.class);
        Join<WatchEvent, MediaItem> mediaJoin = root.join("mediaItem");
        Join<MediaItem, MediaItemMetadata> metadataJoin = mediaJoin.join("metadata", JoinType.LEFT);

        q.select(cb.tuple(
                mediaJoin.get("title").alias("title"),
                metadataJoin.get("posterImageUrl").alias("posterImageUrl"),
                root.get("watchedAt").alias("eventAt"),
                mediaJoin.get("mediaType").alias("mediaType")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        preds.add(completionEquivalent(cb, root));
        addWatchedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.orderBy(latest
                ? cb.desc(root.get("watchedAt"))
                : cb.asc(root.get("watchedAt")));

        return em.createQuery(q).setMaxResults(1).getResultList();
    }

    public static List<Tuple> queryBusiestWatchDay(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<WatchEvent> root = q.from(WatchEvent.class);
        Expression<String> unitKey = watchedUnitKey(cb, root);

        Expression<Long> epochSeconds = cb.quot(
                root.get("watchedAt").as(Long.class),
                cb.literal(1000L)
        ).as(Long.class);

        Expression<String> dateStr = cb.function(
                "strftime", String.class,
                cb.literal("%Y-%m-%d"),
                cb.function("datetime", String.class, epochSeconds, cb.literal("unixepoch"))
        );

        q.select(cb.tuple(
                dateStr.alias("date"),
                cb.countDistinct(unitKey).alias("count")
        ));

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        preds.add(completionEquivalent(cb, root));
        addWatchedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.groupBy(dateStr);
        q.orderBy(cb.desc(cb.countDistinct(unitKey)));

        return em.createQuery(q).setMaxResults(1).getResultList();
    }

    public static List<String> queryDistinctWatchDates(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<WatchEvent> root = q.from(WatchEvent.class);

        Expression<Long> epochSeconds = cb.quot(
                root.get("watchedAt").as(Long.class),
                cb.literal(1000L)
        ).as(Long.class);

        Expression<String> dateStr = cb.function(
                "strftime", String.class,
                cb.literal("%Y-%m-%d"),
                cb.function("datetime", String.class, epochSeconds, cb.literal("unixepoch"))
        );

        q.select(dateStr).distinct(true);

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        preds.add(completionEquivalent(cb, root));
        addWatchedAtRange(cb, preds, root, from, to);
        q.where(preds.toArray(Predicate[]::new));

        q.orderBy(cb.asc(dateStr));

        return em.createQuery(q).getResultList();
    }

    public static List<Integer> queryAvailableRewindYears(EntityManager em, UUID profileId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<MediaItemRating> root = q.from(MediaItemRating.class);

        Expression<Long> epochSeconds = cb.quot(
                root.get("ratedAt").as(Long.class),
                cb.literal(1000L)
        ).as(Long.class);

        Expression<String> year = cb.function(
                "strftime", String.class,
                cb.literal("%Y"),
                cb.function("datetime", String.class, epochSeconds, cb.literal("unixepoch"))
        );

        q.select(year).distinct(true);
        q.where(
                cb.equal(root.get("profile").get("id"), profileId),
                cb.isNotNull(root.get("ratedAt"))
        );
        q.orderBy(cb.desc(year));

        List<String> yearStrings = em.createQuery(q).getResultList();
        List<Integer> years = new java.util.ArrayList<>();
        for (String y : yearStrings) {
            if (y != null) years.add(Integer.parseInt(y));
        }
        return years;
    }

    public static List<Integer> queryAvailableWatchYears(EntityManager em, UUID profileId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<WatchEvent> root = q.from(WatchEvent.class);

        Expression<Long> epochSeconds = cb.quot(
                root.get("watchedAt").as(Long.class),
                cb.literal(1000L)
        ).as(Long.class);

        Expression<String> year = cb.function(
                "strftime", String.class,
                cb.literal("%Y"),
                cb.function("datetime", String.class, epochSeconds, cb.literal("unixepoch"))
        );

        q.select(year).distinct(true);
        q.where(
                cb.equal(root.get("profile").get("id"), profileId),
                cb.isNotNull(root.get("watchedAt"))
        );

        List<String> yearStrings = em.createQuery(q).getResultList();
        List<Integer> years = new java.util.ArrayList<>();
        for (String y : yearStrings) {
            if (y != null) years.add(Integer.parseInt(y));
        }
        return years;
    }

    public static Long queryUniqueItemsWatched(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> q = cb.createQuery(Long.class);
        Root<WatchEvent> root = q.from(WatchEvent.class);
        Expression<String> unitKey = watchedUnitKey(cb, root);

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        preds.add(completionEquivalent(cb, root));
        addWatchedAtRange(cb, preds, root, from, to);

        q.select(cb.countDistinct(unitKey));
        q.where(preds.toArray(Predicate[]::new));

        Long result = em.createQuery(q).getSingleResult();
        return result != null ? result : 0L;
    }

    public static Long queryMoviesWatched(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> q = cb.createQuery(Long.class);
        Root<WatchEvent> root = q.from(WatchEvent.class);

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        preds.add(cb.equal(root.get("mediaItem").get("mediaType"), MediaType.MOVIE));
        preds.add(completionEquivalent(cb, root));
        addWatchedAtRange(cb, preds, root, from, to);

        q.select(cb.countDistinct(root.get("mediaItem").get("id")));
        q.where(preds.toArray(Predicate[]::new));

        Long result = em.createQuery(q).getSingleResult();
        return result != null ? result : 0L;
    }

    public static Long querySeriesWatched(EntityManager em, UUID profileId, Instant from, Instant to) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> q = cb.createQuery(Long.class);
        Root<WatchEvent> root = q.from(WatchEvent.class);

        List<Predicate> preds = new ArrayList<>();
        preds.add(cb.equal(root.get("profile").get("id"), profileId));
        preds.add(cb.equal(root.get("mediaItem").get("mediaType"), MediaType.SERIES));
        preds.add(completionEquivalent(cb, root));
        addWatchedAtRange(cb, preds, root, from, to);

        q.select(cb.countDistinct(root.get("mediaItem").get("id")));
        q.where(preds.toArray(Predicate[]::new));

        Long result = em.createQuery(q).getSingleResult();
        return result != null ? result : 0L;
    }

    private enum HighlightSort {
        FIRST_RATED, LAST_RATED, HIGHEST_RATED, LOWEST_RATED
    }

    /**
     * Controls the primary sort order for people (actors/directors/producers) and genre queries.
     *
     * <ul>
     *   <li>{@link #COUNT} — order by number of rated titles descending, then by average rating
     *       descending as a tiebreaker. Reflects who the profile has watched the most.
     *   <li>{@link #SCORE} — order by average rating descending, then by title count descending
     *       as a tiebreaker. Reflects who the profile rates the highest.
     * </ul>
     */
    public enum SortBy {
        COUNT,
        SCORE
    }
}
