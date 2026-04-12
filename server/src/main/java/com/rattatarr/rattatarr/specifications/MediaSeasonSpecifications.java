package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.entities.MediaSeason;
import com.rattatarr.rattatarr.models.entities.MediaSeasonRating;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

@NullMarked
public class MediaSeasonSpecifications {
    public static Specification<MediaSeason> withRatingForProfile(@Nullable UUID profileId) {
        return (root, query, cb) -> {
            if (profileId == null) {
                return cb.conjunction();
            }

            if (MediaSeason.class.equals(query.getResultType())) {
                Join<MediaSeason, MediaSeasonRating> ratingsJoin = root.join("ratings", JoinType.LEFT);
                ratingsJoin.on(cb.equal(ratingsJoin.get("profile").get("id"), profileId));
                query.distinct(true);
            }

            return cb.conjunction();
        };
    }
}
