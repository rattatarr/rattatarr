package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.entities.Person;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PeopleSpecificationsTest {

    @Test
    void nameLike_shouldReturnSpecificationWhenNameIsNull() {
        Specification<Person> spec = PeopleSpecifications.nameLike(null);
        assertNotNull(spec);
    }

    @Test
    void nameLike_shouldReturnSpecificationWhenNameProvided() {
        Specification<Person> spec = PeopleSpecifications.nameLike("Christopher");
        assertNotNull(spec);
    }
}
