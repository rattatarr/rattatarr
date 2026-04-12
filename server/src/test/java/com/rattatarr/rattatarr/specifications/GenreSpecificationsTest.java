package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.entities.Genre;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class GenreSpecificationsTest {

    @Test
    void nameLike_shouldReturnSpecificationWhenNameIsNull() {
        Specification<Genre> spec = GenreSpecifications.nameLike(null);
        assertNotNull(spec);
    }

    @Test
    void nameLike_shouldReturnSpecificationWhenNameProvided() {
        Specification<Genre> spec = GenreSpecifications.nameLike("Action");
        assertNotNull(spec);
    }
}
