package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.clients.tmdb.TMDbClient;
import com.rattatarr.rattatarr.configs.CacheConfig;
import com.rattatarr.rattatarr.exceptions.ProfilesExceptions;
import com.rattatarr.rattatarr.models.dtos.responses.*;
import com.rattatarr.rattatarr.repositories.ProfilesRepository;
import com.rattatarr.rattatarr.specifications.StatisticsSpecifications;
import com.rattatarr.rattatarr.utils.ValueResolver;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

@Service
public class ProfileStatisticsService {
    protected final Logger logger = LoggerFactory.getLogger(ProfileStatisticsService.class);

    private final EntityManagerFactory entityManagerFactory;
    private final ProfilesRepository profilesRepository;
    private final Executor statisticsExecutor;
    private final TMDbClient tmdbClient;

    public ProfileStatisticsService(
            EntityManagerFactory entityManagerFactory,
            ProfilesRepository profilesRepository,
            @Qualifier("statisticsExecutor") Executor statisticsExecutor,
            TMDbClient tmdbClient) {
        this.entityManagerFactory = entityManagerFactory;
        this.profilesRepository = profilesRepository;
        this.statisticsExecutor = statisticsExecutor;
        this.tmdbClient = tmdbClient;
    }

    @Cacheable(
            value = CacheConfig.STATISTICS_CACHE,
            key = "#profileId + ':' + #ratingThreshold + ':' + #minCount + ':' + #genresLimit + ':' + #actorsLimit + ':' + #directorsLimit + ':' + #producersLimit + ':' + #genreOverTimeLimit + ':' + #profileImageSize"
    )
    public ProfileStatisticsResponseDTO getStatistics(
            UUID profileId,
            Float ratingThreshold,
            Integer minCount,
            Integer genresLimit,
            Integer actorsLimit,
            Integer directorsLimit,
            Integer producersLimit,
            Integer genreOverTimeLimit,
            String profileImageSize) {
        logger.debug(
                "Getting statistics for profile: {}, threshold: {}, minCount: {}, genresLimit: {}, actorsLimit: {}, directorsLimit: {}, producersLimit: {}, genreOverTimeLimit: {}, profileImageSize: {}",
                profileId, ratingThreshold, minCount, genresLimit, actorsLimit, directorsLimit,
                producersLimit, genreOverTimeLimit, profileImageSize);

        profilesRepository
                .findById(profileId)
                .orElseThrow(() -> new ProfilesExceptions.ProfileNotFoundExceptions(profileId));

        CompletableFuture<OverallStatsDTO> overallFuture =
                query("overallStats", em -> getOverallStats(em, profileId));
        CompletableFuture<List<RatingDistributionDTO>> distributionFuture =
                query("ratingDistribution", em -> getRatingDistribution(em, profileId));
        CompletableFuture<List<RatingDistributionDTO>> distributionByIntegerFuture =
                query("ratingDistributionByInteger", em -> getRatingDistributionByInteger(em, profileId));
        CompletableFuture<List<MediaTypeBreakdownDTO>> mediaTypeFuture =
                query("mediaTypeBreakdown", em -> getMediaTypeBreakdown(em, profileId));
        CompletableFuture<List<MediaTypeBreakdownDTO>> jellyfinMediaTypeFuture =
                query("jellyfinMediaTypeBreakdown", em -> getJellyfinMediaTypeBreakdown(em, profileId));
        CompletableFuture<List<GenreStatDTO>> genresByCountFuture =
                query("topGenresByCount", em -> getTopGenresByCount(em, profileId, genresLimit));
        CompletableFuture<List<GenreStatDTO>> genresByScoreFuture =
                query("topGenresByScore", em -> getTopGenresByScore(em, profileId, ratingThreshold, genresLimit));
        CompletableFuture<List<GenreStatDTO>> jellyfinGenresByCountFuture =
                query("jellyfinTopGenresByCount", em -> getJellyfinTopGenresByCount(em, profileId, genresLimit));
        CompletableFuture<List<PersonStatDTO>> directorsByCountFuture =
                query("favoriteDirectorsCount", em -> getFavoriteDirectors(em, profileId, minCount, directorsLimit, profileImageSize, StatisticsSpecifications.SortBy.COUNT));
        CompletableFuture<List<PersonStatDTO>> directorsByScoreFuture =
                query("favoriteDirectorsScore", em -> getFavoriteDirectors(em, profileId, minCount, directorsLimit, profileImageSize, StatisticsSpecifications.SortBy.SCORE));
        CompletableFuture<List<PersonStatDTO>> producersByCountFuture =
                query("favoriteProducersCount", em -> getFavoriteProducers(em, profileId, minCount, producersLimit, profileImageSize, StatisticsSpecifications.SortBy.COUNT));
        CompletableFuture<List<PersonStatDTO>> producersByScoreFuture =
                query("favoriteProducersScore", em -> getFavoriteProducers(em, profileId, minCount, producersLimit, profileImageSize, StatisticsSpecifications.SortBy.SCORE));
        CompletableFuture<List<PersonStatDTO>> actorsByCountFuture =
                query("favoriteActorsCount", em -> getFavoriteActors(em, profileId, minCount, actorsLimit, profileImageSize, StatisticsSpecifications.SortBy.COUNT));
        CompletableFuture<List<PersonStatDTO>> actorsByScoreFuture =
                query("favoriteActorsScore", em -> getFavoriteActors(em, profileId, minCount, actorsLimit, profileImageSize, StatisticsSpecifications.SortBy.SCORE));
        CompletableFuture<List<DecadeStatDTO>> decadesFuture =
                query("decadePreferences", em -> getDecadePreferences(em, profileId));
        CompletableFuture<List<DecadeStatDTO>> jellyfinDecadesFuture =
                query("jellyfinDecadePreferences", em -> getJellyfinDecadePreferences(em, profileId));
        CompletableFuture<List<RecentTrendsDTO>> trendsFuture =
                query("recentTrends", em -> getRecentTrends(em, profileId));
        CompletableFuture<List<RecentTrendsDTO>> jellyfinTrendsFuture =
                query("jellyfinRecentTrends", em -> getJellyfinRecentTrends(em, profileId));
        CompletableFuture<List<RatingActivityDTO>> monthlyFuture =
                query("monthlyActivity", em -> getMonthlyActivity(em, profileId));
        CompletableFuture<RuntimeStatsDTO> runtimeFuture =
                query("runtimeStats", em -> getRuntimeStats(em, profileId));
        CompletableFuture<RuntimeStatsDTO> jellyfinRuntimeFuture =
                query("jellyfinRuntimeStats", em -> getJellyfinRuntimeStats(em, profileId));
        CompletableFuture<RatingConsistencyDTO> consistencyFuture =
                query("ratingConsistency", em -> getRatingConsistency(em, profileId));
        CompletableFuture<List<DayOfWeekActivityDTO>> dayOfWeekFuture =
                query("dayOfWeekActivity", em -> getDayOfWeekActivity(em, profileId));
        CompletableFuture<List<DayOfWeekActivityDTO>> jellyfinDayOfWeekFuture =
                query("jellyfinDayOfWeekActivity", em -> getJellyfinDayOfWeekActivity(em, profileId));
        CompletableFuture<List<RatingHeatmapYearDTO>> heatmapFuture =
                query("ratingHeatmap", em -> getRatingHeatmap(em, profileId));
        CompletableFuture<List<RatingHeatmapYearDTO>> uniqueMediaPlayedHeatmapFuture =
                query("uniqueMediaPlayedHeatmap", em -> getUniqueMediaPlayedHeatmap(em, profileId));
        CompletableFuture<List<GenreOverTimeYearDTO>> genreOverTimeFuture =
                query("genreOverTime", em -> getGenreOverTime(em, profileId, genreOverTimeLimit));
        CompletableFuture<List<GenreOverTimeYearDTO>> jellyfinGenreOverTimeFuture =
                query("jellyfinGenreOverTime", em -> getJellyfinGenreOverTime(em, profileId, genreOverTimeLimit));


        return new ProfileStatisticsResponseDTO(
                overallFuture.join(),
                distributionFuture.join(),
                distributionByIntegerFuture.join(),
                mediaTypeFuture.join(),
                jellyfinMediaTypeFuture.join(),
                genresByCountFuture.join(),
                genresByScoreFuture.join(),
                jellyfinGenresByCountFuture.join(),
                java.util.Collections.emptyList(),
                directorsByCountFuture.join(),
                directorsByScoreFuture.join(),
                producersByCountFuture.join(),
                producersByScoreFuture.join(),
                actorsByCountFuture.join(),
                actorsByScoreFuture.join(),
                decadesFuture.join(),
                jellyfinDecadesFuture.join(),
                trendsFuture.join(),
                jellyfinTrendsFuture.join(),
                monthlyFuture.join(),
                runtimeFuture.join(),
                jellyfinRuntimeFuture.join(),
                consistencyFuture.join(),
                dayOfWeekFuture.join(),
                jellyfinDayOfWeekFuture.join(),
                heatmapFuture.join(),
                uniqueMediaPlayedHeatmapFuture.join(),
                genreOverTimeFuture.join(),
                jellyfinGenreOverTimeFuture.join());
    }

    /**
     * Runs {@code fn} on a fresh, short-lived, read-only {@link EntityManager} obtained from the
     * factory. The EM is created, used, and closed within the same task — safe for concurrent
     * execution without shared transaction context.
     */
    private <T> CompletableFuture<T> query(String label, Function<EntityManager, T> fn) {
        return CompletableFuture.supplyAsync(() -> {
            long start = System.nanoTime();
            try (EntityManager em = entityManagerFactory.createEntityManager()) {
                T result = fn.apply(em);
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                logger.debug("Query '{}' completed in {} ms", label, elapsedMs);
                return result;
            } catch (RuntimeException e) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                logger.debug("Query '{}' failed after {} ms", label, elapsedMs);
                throw e;
            }
        }, statisticsExecutor);
    }

    private OverallStatsDTO getOverallStats(EntityManager em, UUID profileId) {
        Tuple result = StatisticsSpecifications.queryOverallStats(em, profileId);
        Long totalItems = StatisticsSpecifications.queryTotalItems(em);

        Long total = result.get("totalRatings", Long.class);
        Double avg = result.get("averageRating", Double.class);
        Float min = result.get("minRating", Float.class);
        Float max = result.get("maxRating", Float.class);

        return new OverallStatsDTO(
                total != null ? total : 0L,
                totalItems,
                ValueResolver.round2(avg),
                min != null ? min : 0.0f,
                max != null ? max : 0.0f);
    }

    private List<RatingDistributionDTO> getRatingDistribution(EntityManager em, UUID profileId) {
        List<Tuple> results = StatisticsSpecifications.queryRatingDistribution(em, profileId);

        long totalCount = results.stream().mapToLong(t -> t.get("count", Long.class)).sum();

        List<RatingDistributionDTO> distribution = new ArrayList<>();
        for (Tuple tuple : results) {
            Long count = tuple.get("count", Long.class);
            double percentage = totalCount > 0 ? (count * 100.0 / totalCount) : 0.0;
            distribution.add(new RatingDistributionDTO(
                            tuple.get("range", String.class),
                            count,
                            ValueResolver.round2(percentage)
                    )
            );
        }
        return distribution;
    }

    private List<RatingDistributionDTO> getRatingDistributionByInteger(EntityManager em, UUID profileId) {
        List<Tuple> results = StatisticsSpecifications.queryRatingDistributionByInteger(em, profileId);

        Map<Integer, Long> countByBucket = new LinkedHashMap<>();
        for (Tuple tuple : results) {
            Integer bucket = tuple.get("bucket", Integer.class);
            Long count = tuple.get("count", Long.class);
            if (bucket != null) {
                countByBucket.put(bucket, count != null ? count : 0L);
            }
        }

        long totalCount = countByBucket.values().stream().mapToLong(Long::longValue).sum();

        List<RatingDistributionDTO> distribution = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            long count = countByBucket.getOrDefault(i, 0L);
            double percentage = totalCount > 0 ? (count * 100.0 / totalCount) : 0.0;
            distribution.add(new RatingDistributionDTO(
                            String.valueOf(i),
                            count,
                            ValueResolver.round2(percentage)
                    )
            );
        }
        return distribution;
    }

    private List<MediaTypeBreakdownDTO> getMediaTypeBreakdown(EntityManager em, UUID profileId) {
        List<Tuple> results = StatisticsSpecifications.queryMediaTypeBreakdown(em, profileId);
        List<Tuple> totals = StatisticsSpecifications.queryMediaTypeTotal(em);

        Map<String, Long> totalByType = new HashMap<>();
        for (Tuple t : totals) {
            totalByType.put(String.valueOf(t.get("mediaType")), t.get("totalCount", Long.class));
        }

        long totalRated = results.stream().mapToLong(t -> t.get("count", Long.class)).sum();

        List<MediaTypeBreakdownDTO> breakdown = new ArrayList<>();
        for (Tuple tuple : results) {
            Long count = tuple.get("count", Long.class);
            String mediaType = String.valueOf(tuple.get("mediaType"));
            double percentage = totalRated > 0 ? (count * 100.0 / totalRated) : 0.0;
            breakdown.add(new MediaTypeBreakdownDTO(
                            mediaType,
                            count,
                            totalByType.getOrDefault(mediaType, 0L),
                            ValueResolver.round2(percentage),
                            ValueResolver.round2(tuple.get("averageRating", Double.class))
                    )
            );
        }
        return breakdown;
    }

    private List<MediaTypeBreakdownDTO> getJellyfinMediaTypeBreakdown(EntityManager em, UUID profileId) {
        List<Tuple> results = StatisticsSpecifications.queryJellyfinMediaTypeBreakdown(em, profileId);

        long totalWatched = results.stream().mapToLong(t -> t.get("count", Long.class)).sum();

        List<MediaTypeBreakdownDTO> breakdown = new ArrayList<>();
        for (Tuple tuple : results) {
            Long count = tuple.get("count", Long.class);
            String mediaType = String.valueOf(tuple.get("mediaType"));
            double percentage = totalWatched > 0 ? (count * 100.0 / totalWatched) : 0.0;
            breakdown.add(new MediaTypeBreakdownDTO(
                            mediaType,
                            count,
                            count,
                            ValueResolver.round2(percentage),
                            ValueResolver.round2(tuple.get("averageRating", Double.class))
                    )
            );
        }
        return breakdown;
    }

    private List<GenreStatDTO> getTopGenresByCount(EntityManager em, UUID profileId, int limit) {
        List<Tuple> results = StatisticsSpecifications.queryTopGenresByCount(em, profileId, limit);
        return mapGenreTuples(results);
    }

    private List<GenreStatDTO> getTopGenresByScore(EntityManager em, UUID profileId, float ratingThreshold, int limit) {
        List<Tuple> results = StatisticsSpecifications.queryTopGenresByScore(em, profileId, ratingThreshold, limit);
        return mapGenreTuples(results);
    }

    private List<GenreStatDTO> getJellyfinTopGenresByCount(EntityManager em, UUID profileId, int limit) {
        List<Tuple> results = StatisticsSpecifications.queryJellyfinTopGenresByCount(em, profileId, limit);
        return mapGenreTuples(results);
    }

    private List<GenreStatDTO> mapGenreTuples(List<Tuple> results) {
        List<GenreStatDTO> genreStats = new ArrayList<>();
        for (Tuple tuple : results) {
            genreStats.add(new GenreStatDTO(
                    tuple.get("genreName", String.class),
                    tuple.get("count", Long.class),
                    ValueResolver.round2(tuple.get("averageRating", Double.class))));
        }
        return genreStats;
    }

    private List<PersonStatDTO> getFavoriteDirectors(EntityManager em, UUID profileId, int minCount, int limit, String profileImageSize, StatisticsSpecifications.SortBy sortBy) {
        return mapPersonTuples(
                StatisticsSpecifications.queryFavoriteCrewByJob(em, profileId, "Director", minCount, limit, sortBy),
                profileImageSize);
    }

    private List<PersonStatDTO> getFavoriteProducers(EntityManager em, UUID profileId, int minCount, int limit, String profileImageSize, StatisticsSpecifications.SortBy sortBy) {
        return mapPersonTuples(
                StatisticsSpecifications.queryFavoriteCrewByJob(em, profileId, "Producer", minCount, limit, sortBy),
                profileImageSize);
    }

    private List<PersonStatDTO> getFavoriteActors(EntityManager em, UUID profileId, int minCount, int limit, String profileImageSize, StatisticsSpecifications.SortBy sortBy) {
        return mapPersonTuples(StatisticsSpecifications.queryFavoriteActors(em, profileId, minCount, limit, sortBy),
                profileImageSize);
    }

    private List<PersonStatDTO> mapPersonTuples(List<Tuple> results, String profileImageSize) {
        List<PersonStatDTO> people = new ArrayList<>();
        for (Tuple tuple : results) {
            String rawPath = tuple.get("profilePathUrl", String.class);
            String imageUrl = rawPath != null ? tmdbClient.getImageUrl(rawPath, profileImageSize) : null;
            people.add(new PersonStatDTO(
                    tuple.get("personId", UUID.class),
                    tuple.get("name", String.class),
                    imageUrl,
                    ValueResolver.round2(tuple.get("averageRating", Double.class)),
                    tuple.get("itemCount", Long.class)));
        }
        return people;
    }

    private List<DecadeStatDTO> getDecadePreferences(EntityManager em, UUID profileId) {
        List<Tuple> results = StatisticsSpecifications.queryDecadePreferences(em, profileId);

        List<DecadeStatDTO> decadeStats = new ArrayList<>();
        for (Tuple tuple : results) {
            decadeStats.add(new DecadeStatDTO(
                    tuple.get("decade", Integer.class),
                    tuple.get("count", Long.class),
                    ValueResolver.round2(tuple.get("averageRating", Double.class))));
        }
        return decadeStats;
    }

    private List<DecadeStatDTO> getJellyfinDecadePreferences(EntityManager em, UUID profileId) {
        List<Tuple> results = StatisticsSpecifications.queryJellyfinDecadePreferences(em, profileId);

        List<DecadeStatDTO> decadeStats = new ArrayList<>();
        for (Tuple tuple : results) {
            decadeStats.add(new DecadeStatDTO(
                    tuple.get("decade", Integer.class),
                    tuple.get("count", Long.class),
                    ValueResolver.round2(tuple.get("averageRating", Double.class))));
        }
        return decadeStats;
    }

    private List<RecentTrendsDTO> getRecentTrends(EntityManager em, UUID profileId) {
        Instant now = Instant.now();
        return List.of(
                buildTrend(em, profileId, now.minus(30, ChronoUnit.DAYS), now, "30_DAYS"),
                buildTrend(em, profileId, now.minus(90, ChronoUnit.DAYS), now, "90_DAYS"),
                buildTrend(em, profileId, now.minus(365, ChronoUnit.DAYS), now, "365_DAYS"));
    }

    private List<RecentTrendsDTO> getJellyfinRecentTrends(EntityManager em, UUID profileId) {
        Instant now = Instant.now();
        return List.of(
                buildJellyfinTrend(em, profileId, now.minus(30, ChronoUnit.DAYS), now, "30_DAYS"),
                buildJellyfinTrend(em, profileId, now.minus(90, ChronoUnit.DAYS), now, "90_DAYS"),
                buildJellyfinTrend(em, profileId, now.minus(365, ChronoUnit.DAYS), now, "365_DAYS"));
    }

    private RecentTrendsDTO buildTrend(EntityManager em, UUID profileId, Instant start, Instant end, String label) {
        Tuple result = StatisticsSpecifications.queryTrendForPeriod(em, profileId, start, end);
        Long count = result.get("count", Long.class);
        Double avg = result.get("averageRating", Double.class);
        return new RecentTrendsDTO(label, count != null ? count : 0L, ValueResolver.round2(avg));
    }

    private RecentTrendsDTO buildJellyfinTrend(EntityManager em, UUID profileId, Instant start, Instant end, String label) {
        Tuple result = StatisticsSpecifications.queryJellyfinTrendForPeriod(em, profileId, start, end);
        Long count = result.get("count", Long.class);
        Double avg = result.get("averageRating", Double.class);
        return new RecentTrendsDTO(label, count != null ? count : 0L, ValueResolver.round2(avg));
    }

    private List<RatingActivityDTO> getMonthlyActivity(EntityManager em, UUID profileId) {
        List<Tuple> results = StatisticsSpecifications.queryMonthlyActivity(em, profileId);

        List<RatingActivityDTO> activity = new ArrayList<>();
        for (Tuple tuple : results) {
            activity.add(new RatingActivityDTO(
                    tuple.get("period", String.class),
                    tuple.get("count", Long.class),
                    ValueResolver.round2(tuple.get("averageRating", Double.class))));
        }
        return activity;
    }

    private RuntimeStatsDTO getRuntimeStats(EntityManager em, UUID profileId) {
        Tuple result = StatisticsSpecifications.queryRatingRuntimeStats(em, profileId);

        Double avg = result.get("averageRuntime", Double.class);
        Integer longest = result.get("longestRuntime", Integer.class);
        Integer shortest = result.get("shortestRuntime", Integer.class);

        long movieTotal = StatisticsSpecifications.queryRatedMoviesTotalRuntime(em, profileId);
        long seriesTotal = StatisticsSpecifications.queryRatedSeriesRuntimeTotal(em, profileId);

        return new RuntimeStatsDTO(
                avg != null ? (int) Math.round(avg) : 0,
                movieTotal + seriesTotal,
                longest != null ? longest : 0,
                shortest != null ? shortest : 0);
    }

    private RuntimeStatsDTO getJellyfinRuntimeStats(EntityManager em, UUID profileId) {
        Tuple result = StatisticsSpecifications.queryRuntimeStats(em, profileId);

        Double avg = result.get("averageRuntime", Double.class);
        Integer longest = result.get("longestRuntime", Integer.class);
        Integer shortest = result.get("shortestRuntime", Integer.class);

        long movieTotal = StatisticsSpecifications.queryMoviesTotalRuntime(em, profileId);
        long seriesTotal = StatisticsSpecifications.querySeriesRuntimeTotal(em, profileId);

        return new RuntimeStatsDTO(
                avg != null ? (int) Math.round(avg) : 0,
                movieTotal + seriesTotal,
                longest != null ? longest : 0,
                shortest != null ? shortest : 0);
    }

    private RatingConsistencyDTO getRatingConsistency(EntityManager em, UUID profileId) {
        List<Float> ratings = StatisticsSpecifications.queryAllRatings(em, profileId);

        if (ratings.isEmpty()) {
            return new RatingConsistencyDTO(0.0, 0.0, "NO_DATA");
        }

        double mean = ratings.stream().mapToDouble(Float::doubleValue).average().orElse(0.0);
        double variance =
                ratings.stream().mapToDouble(r -> Math.pow(r - mean, 2)).average().orElse(0.0);
        double stdDev = Math.sqrt(variance);

        String level;
        if (stdDev < 1.0) {
            level = "VERY_CONSISTENT";
        } else if (stdDev < 1.5) {
            level = "CONSISTENT";
        } else if (stdDev < 2.0) {
            level = "MODERATE";
        } else if (stdDev < 2.5) {
            level = "VARIED";
        } else {
            level = "VERY_VARIED";
        }

        return new RatingConsistencyDTO(
                Math.round(stdDev * 100.0) / 100.0, Math.round(variance * 100.0) / 100.0, level);
    }

    private List<DayOfWeekActivityDTO> getDayOfWeekActivity(EntityManager em, UUID profileId) {
        List<Tuple> results = StatisticsSpecifications.queryDayOfWeekActivity(em, profileId);

        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        List<DayOfWeekActivityDTO> activity = new ArrayList<>();
        for (Tuple tuple : results) {
            String dayNum = tuple.get("dayOfWeek", String.class);
            if (dayNum != null) {
                activity.add(new DayOfWeekActivityDTO(dayNames[Integer.parseInt(dayNum)],
                        tuple.get("count", Long.class)));
            }
        }
        return activity;
    }

    private List<DayOfWeekActivityDTO> getJellyfinDayOfWeekActivity(EntityManager em, UUID profileId) {
        List<Tuple> results = StatisticsSpecifications.queryJellyfinDayOfWeekActivity(em, profileId);

        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        List<DayOfWeekActivityDTO> activity = new ArrayList<>();
        for (Tuple tuple : results) {
            String dayNum = tuple.get("dayOfWeek", String.class);
            if (dayNum != null) {
                activity.add(new DayOfWeekActivityDTO(dayNames[Integer.parseInt(dayNum)],
                        tuple.get("count", Long.class)));
            }
        }
        return activity;
    }

    private List<RatingHeatmapYearDTO> getRatingHeatmap(EntityManager em, UUID profileId) {
        List<Tuple> results = StatisticsSpecifications.queryRatingHeatmap(em, profileId);

        // Group days by year (first 4 characters of "YYYY-MM-DD"), preserving ascending order.
        Map<Integer, List<RatingHeatmapDayDTO>> byYear = new LinkedHashMap<>();
        for (Tuple tuple : results) {
            String date = tuple.get("date", String.class);
            Long count = tuple.get("count", Long.class);
            if (date == null || count == null) continue;
            int year = Integer.parseInt(date.substring(0, 4));
            byYear.computeIfAbsent(year, y -> new ArrayList<>())
                    .add(new RatingHeatmapDayDTO(date, count));
        }

        List<RatingHeatmapYearDTO> heatmap = new ArrayList<>();
        byYear.forEach((year, days) -> heatmap.add(new RatingHeatmapYearDTO(year, days)));
        return heatmap;
    }

    private List<RatingHeatmapYearDTO> getUniqueMediaPlayedHeatmap(EntityManager em, UUID profileId) {
        List<Tuple> results = StatisticsSpecifications.queryUniqueMediaPlayedHeatmap(em, profileId);

        Map<Integer, List<RatingHeatmapDayDTO>> byYear = new LinkedHashMap<>();
        for (Tuple tuple : results) {
            String date = tuple.get("date", String.class);
            Long count = tuple.get("count", Long.class);
            if (date == null || count == null) continue;
            int year = Integer.parseInt(date.substring(0, 4));
            byYear.computeIfAbsent(year, y -> new ArrayList<>())
                    .add(new RatingHeatmapDayDTO(date, count));
        }

        List<RatingHeatmapYearDTO> heatmap = new ArrayList<>();
        byYear.forEach((year, days) -> heatmap.add(new RatingHeatmapYearDTO(year, days)));
        return heatmap;
    }

    private List<GenreOverTimeYearDTO> getGenreOverTime(EntityManager em, UUID profileId, int limit) {
        List<Tuple> results = StatisticsSpecifications.queryGenreOverTime(em, profileId, limit);

        // Group rows by year, keeping the top-limit genres per year (query already orders by
        // year ASC, count DESC so insertion order gives us the ranked genres).
        Map<Integer, List<GenreYearStatDTO>> byYear = new LinkedHashMap<>();
        for (Tuple tuple : results) {
            String yearStr = tuple.get("year", String.class);
            if (yearStr == null) continue;
            int year = Integer.parseInt(yearStr);
            List<GenreYearStatDTO> genres = byYear.computeIfAbsent(year, y -> new ArrayList<>());
            if (genres.size() < limit) {
                genres.add(new GenreYearStatDTO(
                        tuple.get("genreName", String.class),
                        tuple.get("count", Long.class),
                        ValueResolver.round2(tuple.get("averageRating", Double.class))));
            }
        }

        List<GenreOverTimeYearDTO> overTime = new ArrayList<>();
        byYear.forEach((year, genres) -> overTime.add(new GenreOverTimeYearDTO(year, genres)));
        return overTime;
    }

    private List<GenreOverTimeYearDTO> getJellyfinGenreOverTime(EntityManager em, UUID profileId, int limit) {
        List<Tuple> results = StatisticsSpecifications.queryJellyfinGenreOverTime(em, profileId, limit);

        Map<Integer, List<GenreYearStatDTO>> byYear = new LinkedHashMap<>();
        for (Tuple tuple : results) {
            String yearStr = tuple.get("year", String.class);
            if (yearStr == null) continue;
            int year = Integer.parseInt(yearStr);
            List<GenreYearStatDTO> genres = byYear.computeIfAbsent(year, y -> new ArrayList<>());
            if (genres.size() < limit) {
                genres.add(new GenreYearStatDTO(
                        tuple.get("genreName", String.class),
                        tuple.get("count", Long.class),
                        ValueResolver.round2(tuple.get("averageRating", Double.class))));
            }
        }

        List<GenreOverTimeYearDTO> overTime = new ArrayList<>();
        byYear.forEach((year, genres) -> overTime.add(new GenreOverTimeYearDTO(year, genres)));
        return overTime;
    }
}
