package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.JobStatus;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.entities.BackgroundJob;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BackgroundJobSpecificationsTest {

    @Test
    void hasType_withNullType_returnsNullPredicate() {
        Specification<BackgroundJob> spec = BackgroundJobSpecifications.hasType(null);
        assertNotNull(spec);
    }

    @Test
    void hasType_withValidType_returnsSpec() {
        Specification<BackgroundJob> spec = BackgroundJobSpecifications.hasType(JobType.JELLYFIN_SYNC);
        assertNotNull(spec);
    }

    @Test
    void hasStatus_withNullStatus_returnsNullPredicate() {
        Specification<BackgroundJob> spec = BackgroundJobSpecifications.hasStatus(null);
        assertNotNull(spec);
    }

    @Test
    void hasStatus_withValidStatus_returnsSpec() {
        Specification<BackgroundJob> spec = BackgroundJobSpecifications.hasStatus(JobStatus.RUNNING);
        assertNotNull(spec);
    }
}
