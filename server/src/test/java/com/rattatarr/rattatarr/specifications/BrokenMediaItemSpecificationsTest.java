package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.entities.BrokenMediaItem;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BrokenMediaItemSpecificationsTest {

    @Test
    void isMovie_shouldReturnSpecification() {
        Specification<BrokenMediaItem> spec = BrokenMediaItemSpecifications.isMovie();
        assertNotNull(spec);
    }

    @Test
    void isSeries_shouldReturnSpecification() {
        Specification<BrokenMediaItem> spec = BrokenMediaItemSpecifications.isSeries();
        assertNotNull(spec);
    }

    @Test
    void notResolved_shouldReturnSpecification() {
        Specification<BrokenMediaItem> spec = BrokenMediaItemSpecifications.notResolved();
        assertNotNull(spec);
    }
}
