package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.entities.Person;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

@NullMarked
public class PeopleSpecifications {
    public static Specification<Person> nameLike(@Nullable String name) {
        return (root, query, cb) -> name == null ? null :
                cb.like(cb.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%");
    }
}
