package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.entities.MediaSeason;
import com.rattatarr.rattatarr.models.entities.MediaSeasonRating;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.repositories.MediaSeasonRatingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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

//
//    /**
//     * Delete a rating for a profile and media season
//     */
//    public boolean deleteRating(UUID profileId, UUID mediaSeasonId) {
//        Profile profile = profilesService.findById(profileId)
//                .orElseThrow(() -> new CommonExceptions.ResourceNotFoundExceptions("Profile not found: " + profileId));
//        MediaSeason mediaSeason = mediaSeasonsService.findById(mediaSeasonId)
//                .orElseThrow(() -> new CommonExceptions.ResourceNotFoundExceptions("Media season not found: " + mediaSeasonId));
//
//        Optional<MediaSeasonRating> rating = repository.findByProfileAndMediaSeason(profile, mediaSeason);
//        if (rating.isPresent()) {
//            logger.info("Deleting rating for profile {} and media season {}", profileId, mediaSeasonId);
//            repository.delete(rating.get());
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * Get a rating for a specific profile and media season
//     */
//    @Transactional(readOnly = true)
//    public Optional<MediaSeasonRating> getRatingByProfileAndMediaSeason(UUID profileId, UUID mediaSeasonId) {
//        return repository.findAll(
//                MediaSeasonRatingSpecifications.byProfileAndMediaSeason(profileId, mediaSeasonId)
//        ).stream().findFirst();
//    }
//
//    /**
//     * Get all ratings for a profile
//     */
//    @Transactional(readOnly = true)
//    public List<MediaSeasonRating> getAllRatingsForProfile(UUID profileId) {
//        Profile profile = profilesService.findById(profileId)
//                .orElseThrow(() -> new CommonExceptions.ResourceNotFoundExceptions("Profile not found: " + profileId));
//        return repository.findAllByProfile(profile);
//    }
//
//    /**
//     * Get all ratings for a media season
//     */
//    @Transactional(readOnly = true)
//    public List<MediaSeasonRating> getAllRatingsForMediaSeason(UUID mediaSeasonId) {
//        MediaSeason mediaSeason = mediaSeasonsService.findById(mediaSeasonId)
//                .orElseThrow(() -> new CommonExceptions.ResourceNotFoundExceptions("Media season not found: " + mediaSeasonId));
//        return repository.findAllByMediaSeason(mediaSeason);
//    }
//
//    /**
//     * Calculate average rating for a media season
//     */
//    @Transactional(readOnly = true)
//    public Optional<BigDecimal> getAverageRatingForMediaSeason(UUID mediaSeasonId) {
//        List<MediaSeasonRating> ratings = repository.findAll(
//                MediaSeasonRatingSpecifications.byMediaSeason(mediaSeasonId)
//        );
//
//        if (ratings.isEmpty()) {
//            return Optional.empty();
//        }
//
//        BigDecimal sum = ratings.stream()
//                .map(MediaSeasonRating::rating)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        BigDecimal average = sum.divide(
//                new BigDecimal(ratings.size()),
//                1,
//                RoundingMode.HALF_UP
//        );
//
//        return Optional.of(average);
//    }
//
//    /**
//     * Batch fetch ratings for multiple seasons (N+1 prevention)
//     * Returns a map of mediaSeasonId -> rating
//     */
//    @Transactional(readOnly = true)
//    public Map<UUID, BigDecimal> getRatingsForSeasons(List<UUID> mediaSeasonIds, UUID profileId) {
//        if (mediaSeasonIds.isEmpty()) {
//            return Map.of();
//        }
//
//        List<MediaSeasonRating> ratings = repository.findAllByProfileIdAndMediaSeasonIdIn(profileId, mediaSeasonIds);
//
//        Map<UUID, BigDecimal> ratingsMap = new HashMap<>();
//        for (MediaSeasonRating rating : ratings) {
//            ratingsMap.put(rating.mediaSeason().id(), rating.rating());
//        }
//
//        return ratingsMap;
//    }
//
//    /**
//     * Get all season ratings for a series (all seasons of a media item)
//     */
//    @Transactional(readOnly = true)
//    public List<MediaSeasonRating> getAllRatingsForSeries(UUID mediaItemId, UUID profileId) {
//        // This would require fetching all seasons for the media item first
//        // Then getting ratings for those seasons
//        // Implementation will be simpler once we have the full integration
//        throw new UnsupportedOperationException("Not yet implemented - will be added in Phase 6");
//    }
}
