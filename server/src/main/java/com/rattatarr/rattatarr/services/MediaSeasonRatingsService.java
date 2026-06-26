package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.exceptions.CommonExceptions;
import com.rattatarr.rattatarr.models.entities.MediaSeason;
import com.rattatarr.rattatarr.models.entities.MediaSeasonRating;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.repositories.MediaSeasonRatingsRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@Transactional
public class MediaSeasonRatingsService extends BaseService<MediaSeasonRating, MediaSeasonRatingsRepository> {
    public MediaSeasonRatingsService(
            MediaSeasonRatingsRepository repository
    ) {
        super(repository);
    }

    @Override
    protected void updateEntity(MediaSeasonRating existing, MediaSeasonRating updated) {
        existing.setRating(updated.rating());
    }

    @Transactional(readOnly = true)
    public Optional<MediaSeasonRating> findByProfileAndMediaSeason(
            Profile profile,
            MediaSeason mediaSeason
    ) {
        return repository.findByProfileAndMediaSeason(profile, mediaSeason);
    }

    @Transactional
    public MediaSeasonRating updateExistingReview(
            Profile profile,
            MediaSeason mediaSeason,
            Consumer<MediaSeasonRating> mutator
    ) {
        MediaSeasonRating rating = repository.findByProfileAndMediaSeason(profile, mediaSeason)
                .orElseThrow(() -> new CommonExceptions.ResourceNotFoundExceptions(
                        "Cannot review a season that has not been rated yet"));
        mutator.accept(rating);
        return repository.save(rating);
    }

    @Transactional(readOnly = true)
    public Map<UUID, Float> batchFetchRatingsMap(List<UUID> seasonIds, @Nullable UUID profileId) {
        if (profileId == null || seasonIds.isEmpty()) {
            return Map.of();
        }
        List<MediaSeasonRating> ratings = repository.findAllByProfileIdAndMediaSeasonIdIn(profileId, seasonIds);
        return ratings.stream()
                .collect(Collectors.toMap(r -> r.mediaSeason().id(), MediaSeasonRating::rating));
    }

    /**
     * Like {@link #batchFetchRatingsMap} but returns the full rating entities so reviews can be read.
     */
    @Transactional(readOnly = true)
    public Map<UUID, MediaSeasonRating> batchFetchRatingEntitiesMap(List<UUID> seasonIds, @Nullable UUID profileId) {
        if (profileId == null || seasonIds.isEmpty()) {
            return Map.of();
        }
        List<MediaSeasonRating> ratings = repository.findAllByProfileIdAndMediaSeasonIdIn(profileId, seasonIds);
        return ratings.stream()
                .collect(Collectors.toMap(r -> r.mediaSeason().id(), r -> r));
    }

    @Transactional
    public MediaSeasonRating upsert(
            Profile profile,
            MediaSeason mediaSeason,
            Float rating
    ) {
        MediaSeasonRating existingRating = this.findByProfileAndMediaSeason(profile, mediaSeason)
                .orElse(null);

        MediaSeasonRating ratingToSave = new MediaSeasonRating(
                profile,
                mediaSeason,
                rating
        );

        this.upsert(existingRating, ratingToSave);

        return ratingToSave;
    }
}
