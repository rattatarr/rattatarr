package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.entities.BrokenMediaItem;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.domain.Specification;

@NullMarked
public class BrokenMediaItemSpecifications {
    public static Specification<BrokenMediaItem> isMovie() {
        return (root, query, cb) -> cb.equal(root.get("mediaType"), "MOVIE");
    }

    public static Specification<BrokenMediaItem> isSeries() {
        return (root, query, cb) -> cb.equal(root.get("mediaType"), "SERIES");
    }

    public static Specification<BrokenMediaItem> notResolved() {
        return (root, query, cb) -> cb.isFalse(root.get("resolved"));
    }
}
