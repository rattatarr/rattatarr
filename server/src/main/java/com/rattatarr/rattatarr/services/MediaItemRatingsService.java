package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.RatedItemSummary;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemRating;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.repositories.MediaItemRatingsRepository;
import com.rattatarr.rattatarr.specifications.StatisticsSpecifications;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Tuple;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MediaItemRatingsService extends BaseService<MediaItemRating, MediaItemRatingsRepository> {
    private final EntityManagerFactory entityManagerFactory;

    public MediaItemRatingsService(
            MediaItemRatingsRepository repository,
            EntityManagerFactory entityManagerFactory
    ) {
        super(repository);
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    protected void updateEntity(MediaItemRating existing, MediaItemRating updated) {
        existing.setRating(updated.rating());
    }

    @Transactional(readOnly = true)
    public Optional<MediaItemRating> findByProfileAndMediaItem(
            Profile profile,
            MediaItem mediaItem
    ) {
        return repository.findByProfileAndMediaItem(profile, mediaItem);
    }

    @Transactional(readOnly = true)
    public List<MediaItemRating> findAllByProfile(Profile profile) {
        return repository.findAllByProfile(profile);
    }

    @Transactional
    public MediaItemRating upsert(
            Profile profile,
            MediaItem mediaItem,
            Float rating
    ) {
        MediaItemRating existingRating = this.findByProfileAndMediaItem(profile, mediaItem)
                .orElse(null);

        MediaItemRating ratingToSave = new MediaItemRating(
                profile,
                mediaItem,
                rating,
                Instant.now()
        );

        this.upsert(existingRating, ratingToSave);

        return ratingToSave;
    }

    @Transactional
    public MediaItemRating upsert(
            Profile profile,
            MediaItem mediaItem,
            Float rating,
            Instant ratedAt
    ) {
        MediaItemRating existingRating = this.findByProfileAndMediaItem(profile, mediaItem)
                .orElse(null);

        MediaItemRating ratingToSave = new MediaItemRating(
                profile,
                mediaItem,
                rating,
                ratedAt
        );

        this.upsert(existingRating, ratingToSave);

        return ratingToSave;
    }

    @Transactional(readOnly = true)
    public List<RatedItemSummary> findAllRatedByProfileRecentFirst(Profile profile) {
        return repository.findAllByProfileOrderByRatedAtDesc(profile).stream()
                .map(r -> new RatedItemSummary(
                        r.mediaItem().title(),
                        r.mediaItem().mediaType().name(),
                        r.rating()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RatedItemSummary> findTop15ByProfileOrderByRatingDesc(Profile profile) {
        return repository.findTop15ByProfileOrderByRatingDesc(profile).stream()
                .map(r -> new RatedItemSummary(
                        r.mediaItem().title(),
                        r.mediaItem().mediaType().name(),
                        r.rating()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Tuple> findTopGenresByWatchCount(UUID profileId, int limit) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return StatisticsSpecifications.queryTopGenresBy(em, profileId, 0f, limit, StatisticsSpecifications.SortBy.COUNT);
        }
    }

    @Transactional(readOnly = true)
    public List<Tuple> findTopGenresByAverageRating(UUID profileId, float ratingThreshold, int limit) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return StatisticsSpecifications.queryTopGenresBy(em, profileId, ratingThreshold, limit, StatisticsSpecifications.SortBy.SCORE);
        }
    }

    @Transactional(readOnly = true)
    public List<Tuple> findJellyfinTopGenresByCount(UUID profileId, int limit) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return StatisticsSpecifications.queryJellyfinTopGenresBy(em, profileId, limit, StatisticsSpecifications.SortBy.COUNT);
        }
    }

    @Transactional(readOnly = true)
    public List<Tuple> findTopActors(UUID profileId, int minCount, int limit, StatisticsSpecifications.SortBy sortBy) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return StatisticsSpecifications.queryFavoriteActors(em, profileId, minCount, limit, sortBy);
        }
    }

    @Transactional(readOnly = true)
    public List<Tuple> findTopDirectors(UUID profileId, int minCount, int limit, StatisticsSpecifications.SortBy sortBy) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return StatisticsSpecifications.queryFavoriteCrewByJob(em, profileId, "Director", minCount, limit, sortBy);
        }
    }

    @Transactional(readOnly = true)
    public List<Tuple> findTopProducers(UUID profileId, int minCount, int limit, StatisticsSpecifications.SortBy sortBy) {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            return StatisticsSpecifications.queryFavoriteCrewByJob(em, profileId, "Producer", minCount, limit, sortBy);
        }
    }

    @Transactional(readOnly = true)
    public Map<UUID, MediaItemRating> batchFetchRatingsMap(List<UUID> mediaItemIds, @Nullable UUID profileId) {
        if (profileId == null || mediaItemIds.isEmpty()) {
            return Map.of();
        }

        List<MediaItemRating> ratings = repository.findAllByProfileIdAndMediaItemIdIn(profileId, mediaItemIds);

        return ratings.stream()
                .collect(Collectors.toMap(r -> r.mediaItem().id(), r -> r));
    }
}
