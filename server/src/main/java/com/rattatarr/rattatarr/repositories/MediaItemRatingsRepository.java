package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemRating;
import com.rattatarr.rattatarr.models.entities.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaItemRatingsRepository extends BaseRepository<MediaItemRating> {
    Optional<MediaItemRating> findByProfileAndMediaItem(Profile profile, MediaItem mediaItem);

    List<MediaItemRating> findAllByProfile(Profile profile);

    List<MediaItemRating> findAllByMediaItem(MediaItem mediaItem);

    @Query("SELECT r FROM MediaItemRating r WHERE r.profile.id = :profileId AND r.mediaItem.id IN :mediaItemIds")
    List<MediaItemRating> findAllByProfileIdAndMediaItemIdIn(
            @Param("profileId") UUID profileId,
            @Param("mediaItemIds") List<UUID> mediaItemIds
    );

    List<MediaItemRating> findTop15ByProfileOrderByRatingDesc(Profile profile);

    List<MediaItemRating> findAllByProfileOrderByRatedAtDesc(Profile profile);
}
