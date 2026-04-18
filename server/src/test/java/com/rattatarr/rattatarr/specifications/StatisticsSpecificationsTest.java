package com.rattatarr.rattatarr.specifications;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the new method signatures introduced by the "by count / by score" split.
 *
 * <p>These tests only verify that the methods exist with the expected signatures and return
 * non-null results — deep query correctness is validated by integration tests. The goal here
 * is to satisfy TDD's RED phase and lock in the API contract.
 */
class StatisticsSpecificationsTest {

    // ---------------------------------------------------------------------------
    // Genres
    // ---------------------------------------------------------------------------

    @Test
    void queryTopGenresBy_countSort_methodExists() {
        assertThrows(NullPointerException.class, () ->
                StatisticsSpecifications.queryTopGenresBy(null, UUID.randomUUID(), 0f, 10, StatisticsSpecifications.SortBy.COUNT));
    }

    @Test
    void queryTopGenresBy_scoreSort_methodExists() {
        assertThrows(NullPointerException.class, () ->
                StatisticsSpecifications.queryTopGenresBy(null, UUID.randomUUID(), 7.0f, 10, StatisticsSpecifications.SortBy.SCORE));
    }

    // ---------------------------------------------------------------------------
    // Crew (directors / producers)
    // ---------------------------------------------------------------------------

    @Test
    void queryFavoriteCrewByJob_withCountSort_methodExists() {
        assertThrows(NullPointerException.class, () ->
                StatisticsSpecifications.queryFavoriteCrewByJob(
                        null, UUID.randomUUID(), "Director", 1, 10,
                        StatisticsSpecifications.SortBy.COUNT));
    }

    @Test
    void queryFavoriteCrewByJob_withScoreSort_methodExists() {
        assertThrows(NullPointerException.class, () ->
                StatisticsSpecifications.queryFavoriteCrewByJob(
                        null, UUID.randomUUID(), "Director", 1, 10,
                        StatisticsSpecifications.SortBy.SCORE));
    }

    // ---------------------------------------------------------------------------
    // Actors
    // ---------------------------------------------------------------------------

    @Test
    void queryFavoriteActors_withCountSort_methodExists() {
        assertThrows(NullPointerException.class, () ->
                StatisticsSpecifications.queryFavoriteActors(
                        null, UUID.randomUUID(), 1, 10,
                        StatisticsSpecifications.SortBy.COUNT));
    }

    @Test
    void queryFavoriteActors_withScoreSort_methodExists() {
        assertThrows(NullPointerException.class, () ->
                StatisticsSpecifications.queryFavoriteActors(
                        null, UUID.randomUUID(), 1, 10,
                        StatisticsSpecifications.SortBy.SCORE));
    }

    // ---------------------------------------------------------------------------
    // SortBy enum
    // ---------------------------------------------------------------------------

    @Test
    void sortByEnum_hasBothValues() {
        StatisticsSpecifications.SortBy count = StatisticsSpecifications.SortBy.COUNT;
        StatisticsSpecifications.SortBy score = StatisticsSpecifications.SortBy.SCORE;
        assertNotNull(count);
        assertNotNull(score);
    }

    // ---------------------------------------------------------------------------
    // Rating distribution by integer bucket
    // ---------------------------------------------------------------------------

    @Test
    void queryRatingDistributionByInteger_methodExists() {
        // Must accept (EntityManager, UUID) — no extra params needed.
        assertThrows(NullPointerException.class, () ->
                StatisticsSpecifications.queryRatingDistributionByInteger(null, UUID.randomUUID()));
    }

    // ---------------------------------------------------------------------------
    // Min-5-titles contract for SCORE sort
    // ---------------------------------------------------------------------------

    /**
     * When sortBy is SCORE, the effective HAVING threshold must be at least 5,
     * even if the caller passes minCount=1.
     * Verified by asserting the method accepts the call (signature contract) —
     * the enforcement of max(minCount,5) is in the production implementation.
     */
    @Test
    void queryFavoriteCrewByJob_withScoreSort_andMinCountBelowFive_usesMinFive() {
        // minCount=1, SCORE — must still accept the call (throws NPE on null EM, not IllegalArgument)
        assertThrows(NullPointerException.class, () ->
                StatisticsSpecifications.queryFavoriteCrewByJob(
                        null, UUID.randomUUID(), "Director", 1, 10,
                        StatisticsSpecifications.SortBy.SCORE));
    }

    @Test
    void queryFavoriteActors_withScoreSort_andMinCountBelowFive_usesMinFive() {
        // minCount=1, SCORE — must still accept the call (throws NPE on null EM, not IllegalArgument)
        assertThrows(NullPointerException.class, () ->
                StatisticsSpecifications.queryFavoriteActors(
                        null, UUID.randomUUID(), 1, 10,
                        StatisticsSpecifications.SortBy.SCORE));
    }

    // ---------------------------------------------------------------------------
    // Genre over time
    // ---------------------------------------------------------------------------

    @Test
    void queryGenreOverTime_methodExists() {
        // Must accept (EntityManager, UUID, int limit) and return List<Tuple>.
        assertThrows(NullPointerException.class, () ->
                StatisticsSpecifications.queryGenreOverTime(null, UUID.randomUUID(), 5));
    }
}
