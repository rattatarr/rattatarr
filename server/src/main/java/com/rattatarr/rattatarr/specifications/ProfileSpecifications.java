package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.entities.Profile;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.domain.Specification;

@NullMarked
public class ProfileSpecifications {

    public static Specification<Profile> nameLike(String name) {
        return (root, query, cb) -> name == null ? null :
                cb.like(cb.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%");
    }

}
