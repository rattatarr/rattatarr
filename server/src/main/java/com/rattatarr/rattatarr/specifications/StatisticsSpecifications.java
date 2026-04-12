package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.MediaType;
import com.rattatarr.rattatarr.models.entities.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * All queries are read-only and expect a transactional context to be managed by the caller.
 */
public final class StatisticsSpecifications {
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

    public static Tuple queryOverallStats(EntityManager em, UUID profileId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);

        q.select(cb.tuple(
                cb.count(root).alias("totalRatings"),
                cb.avg(root.get("rating")).alias("averageRating"),
                cb.min(root.get("rating")).alias("minRating"),
                cb.max(root.get("rating")).alias("maxRating")
        ));
        q.where(cb.equal(root.get("profile").get("id"), profileId));

        return em.createQuery(q).getSingleResult();
    }

    // -------------------------------------------------------------------------
    // Genres
    // -------------------------------------------------------------------------

    /**
     * Secondary sort: average rating descending.
     */
    public static List<Tuple> queryTopGenresByCount(EntityManager em, UUID profileId, int limit) {
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
        q.where(cb.equal(root.get("profile").get("id"), profileId));
        q.groupBy(genreJoin.get("name"));
        q.orderBy(cb.desc(cb.count(root)), cb.desc(cb.avg(root.get("rating"))));

        return em.createQuery(q).setMaxResults(limit).getResultList();
    }


    /**
     * Secondary sort: title count descending.
     */
    public static List<Tuple> queryTopGenresByScore(EntityManager em, UUID profileId, float ratingThreshold, int limit) {
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
        q.where(
                cb.equal(root.get("profile").get("id"), profileId),
                cb.greaterThanOrEqualTo(root.get("rating"), ratingThreshold)
        );
        q.groupBy(genreJoin.get("name"));
        q.orderBy(cb.desc(cb.avg(root.get("rating"))), cb.desc(cb.count(root)));

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

    // -------------------------------------------------------------------------
    // People (directors, producers, actors)
    // -------------------------------------------------------------------------

    public static List<Tuple> queryFavoriteCrewByJob(
            EntityManager em, UUID profileId, String job, int minCount, int limit, SortBy sortBy) {
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
        q.where(
                cb.equal(root.get("profile").get("id"), profileId),
                cb.equal(crewJoin.get("job"), job)
        );
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


    public static List<Tuple> queryFavoriteActors(EntityManager em, UUID profileId, int minCount, int limit, SortBy sortBy) {
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
        q.where(cb.equal(root.get("profile").get("id"), profileId));
        q.groupBy(personJoin.get("id"), personJoin.get("name"), personJoin.get("profilePathUrl"));
        // When sorting by score, require at least 5 rated titles to avoid one-hit 10/10 outliers.
        long effectiveMinCountActors = sortBy == SortBy.SCORE ? Math.max(minCount, 5) : minCount;
        q.having(cb.greaterThanOrEqualTo(cb.countDistinct(mediaJoin.get("id")), effectiveMinCountActors));
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

    public static List<Tuple> queryRatingDistribution(EntityManager em, UUID profileId) {
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
        q.where(cb.equal(root.get("profile").get("id"), profileId));
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
    public static List<Tuple> queryRatingDistributionByInteger(EntityManager em, UUID profileId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);

        Expression<Integer> bucket = cb.function("ROUND", Integer.class, root.get("rating"));

        q.select(cb.tuple(
                bucket.alias("bucket"),
                cb.count(root).alias("count")
        ));
        q.where(cb.equal(root.get("profile").get("id"), profileId));
        q.groupBy(bucket);
        q.orderBy(cb.asc(bucket));

        return em.createQuery(q).getResultList();
    }

    public static List<Tuple> queryMediaTypeBreakdown(EntityManager em, UUID profileId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);
        Join<MediaItemRating, MediaItem> mediaJoin = root.join("mediaItem");

        q.select(cb.tuple(
                mediaJoin.get("mediaType").alias("mediaType"),
                cb.count(root).alias("count"),
                cb.avg(root.get("rating")).alias("averageRating")
        ));
        q.where(cb.equal(root.get("profile").get("id"), profileId));
        q.groupBy(mediaJoin.get("mediaType"));

        return em.createQuery(q).getResultList();
    }


    public static List<Tuple> queryDecadePreferences(EntityManager em, UUID profileId) {
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
        q.where(
                cb.equal(root.get("profile").get("id"), profileId),
                cb.isNotNull(mediaJoin.get("productionYear"))
        );
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

    public static List<Tuple> queryDayOfWeekActivity(EntityManager em, UUID profileId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<MediaItemRating> root = q.from(MediaItemRating.class);

        // SQLite stores Instant as epoch milliseconds — convert to seconds for datetime()
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
        q.where(cb.equal(root.get("profile").get("id"), profileId));
        q.groupBy(dayOfWeek);
        q.orderBy(cb.asc(dayOfWeek));

        return em.createQuery(q).getResultList();
    }

    // -------------------------------------------------------------------------
    // Runtime
    // -------------------------------------------------------------------------

    /**
     * Aggregate runtime stats (average, longest, shortest) across all rated items that have a
     * runtimeMinutes value. For series the runtimeMinutes field holds the average episode duration;
     * use {@link #queryMoviesTotalRuntime} and {@link #querySeriesRuntimeTotal} to compute the
     * correct total watched time.
     */
    public static Tuple queryRuntimeStats(EntityManager em, UUID profileId) {
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


    /**
     * Total runtime (minutes) for rated movies. Movies store their full runtime directly in
     * {@code runtimeMinutes}, so this is a simple SUM.
     */
    public static Long queryMoviesTotalRuntime(EntityManager em, UUID profileId) {
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
     * Total runtime (minutes) for rated series. Series store an average episode duration in
     * {@code runtimeMinutes}; total watched time is {@code runtimeMinutes × episodeCount} per
     * series, summed across all rated series for the profile.
     *
     * <p>Uses two separate queries to avoid a Cartesian explosion from joining ratings to seasons
     * to episodes in a single query. Step 1 fetches rated series with their per-episode runtime.
     * Step 2 counts episodes per series. The multiplication happens in Java.
     *
     * <p>Series without a {@code runtimeMinutes} value or without any episodes are excluded.
     */
    public static Long querySeriesRuntimeTotal(EntityManager em, UUID profileId) {
        // Step 1: rated series (seriesId → runtimeMinutes per episode)
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

        // Collect IDs and runtime map
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

        // Step 2: episode count per series
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
