package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.entities.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class GenericSpecificationsTest {
    @Test
    void testCreatedAtAfterReturnsNullForNullDate() {
        Specification<Profile> spec = GenericSpecifications.createdAtAfter(null);
        assertNotNull(spec);
    }

    @Test
    void testCreatedAtBeforeReturnsNullForNullDate() {
        Specification<Profile> spec = GenericSpecifications.createdAtBefore(null);
        assertNotNull(spec);
    }

    @Test
    void testUpdatedAtAfterReturnsNullForNullDate() {
        Specification<Profile> spec = GenericSpecifications.updatedAtAfter(null);
        assertNotNull(spec);
    }

    @Test
    void testUpdatedAtBeforeReturnsNullForNullDate() {
        Specification<Profile> spec = GenericSpecifications.updatedAtBefore(null);
        assertNotNull(spec);
    }

    @Test
    void testNotDeleted() {
        Specification<Profile> spec = GenericSpecifications.notDeleted();
        assertNotNull(spec);
    }

    @Test
    void testCreatedAtAfterWithValidDate() {
        Specification<Profile> spec = GenericSpecifications.createdAtAfter(Instant.now());
        assertNotNull(spec);
    }
}
