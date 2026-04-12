package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.entities.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProfileSpecificationsTest {
    @Test
    void testNameLikeWithValidName() {
        Specification<Profile> spec = ProfileSpecifications.nameLike("Test");
        assertNotNull(spec);
    }

    @Test
    void testNameLikeWithNullName() {
        Specification<Profile> spec = ProfileSpecifications.nameLike(null);
        assertNotNull(spec);
    }
}
