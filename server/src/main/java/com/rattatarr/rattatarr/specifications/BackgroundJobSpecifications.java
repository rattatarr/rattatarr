package com.rattatarr.rattatarr.specifications;

import com.rattatarr.rattatarr.models.JobStatus;
import com.rattatarr.rattatarr.models.JobType;
import com.rattatarr.rattatarr.models.entities.BackgroundJob;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.domain.Specification;

@NullMarked
public class BackgroundJobSpecifications {

    public static Specification<BackgroundJob> hasType(JobType type) {
        return (root, query, cb) -> type == null ? null :
                cb.equal(root.get("type"), type);
    }

    public static Specification<BackgroundJob> hasStatus(JobStatus status) {
        return (root, query, cb) -> status == null ? null :
                cb.equal(root.get("status"), status);
    }
}
