package com.rattatarr.rattatarr.services;

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

    @Transactional(readOnly = true)
    public Map<UUID, Float> batchFetchRatingsMap(List<UUID> seasonIds, @Nullable UUID profileId) {
        if (profileId == null || seasonIds.isEmpty()) {
            return Map.of();
        }
        List<MediaSeasonRating> ratings = repository.findAllByProfileIdAndMediaSeasonIdIn(profileId, seasonIds);
        return ratings.stream()
                .collect(Collectors.toMap(r -> r.mediaSeason().id(), MediaSeasonRating::rating));
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
