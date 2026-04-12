package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemRating;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.repositories.MediaItemRatingsRepository;
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
    public MediaItemRatingsService(
            MediaItemRatingsRepository repository
    ) {
        super(repository);
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
    public Map<UUID, MediaItemRating> batchFetchRatingsMap(List<UUID> mediaItemIds, @Nullable UUID profileId) {
        if (profileId == null || mediaItemIds.isEmpty()) {
            return Map.of();
        }

        List<MediaItemRating> ratings = repository.findAllByProfileIdAndMediaItemIdIn(profileId, mediaItemIds);

        return ratings.stream()
                .collect(Collectors.toMap(r -> r.mediaItem().id(), r -> r));
    }
}
