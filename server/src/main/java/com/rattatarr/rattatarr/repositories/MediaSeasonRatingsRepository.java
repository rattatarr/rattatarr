package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.entities.MediaSeason;
import com.rattatarr.rattatarr.models.entities.MediaSeasonRating;
import com.rattatarr.rattatarr.models.entities.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaSeasonRatingsRepository extends BaseRepository<MediaSeasonRating> {
    Optional<MediaSeasonRating> findByProfileAndMediaSeason(Profile profile, MediaSeason mediaSeason);

    List<MediaSeasonRating> findAllByProfile(Profile profile);

    List<MediaSeasonRating> findAllByMediaSeason(MediaSeason mediaSeason);

    @Query("SELECT r FROM MediaSeasonRating r WHERE r.profile.id = :profileId AND r.mediaSeason.id IN :mediaSeasonIds")
    List<MediaSeasonRating> findAllByProfileIdAndMediaSeasonIdIn(
            @Param("profileId") UUID profileId,
            @Param("mediaSeasonIds") List<UUID> mediaSeasonIds
    );
}
