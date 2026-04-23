-- SQLite does not support ALTER TABLE DROP/ADD CONSTRAINT.
-- Recreate background_jobs with updated CHECK constraint to include RADARR_RATINGS_REFRESH.

CREATE TABLE background_jobs_new (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    finished_at timestamp,
    message varchar(255),
    profile_id blob,
    started_at timestamp,
    status varchar(255) not null check (status in ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    type varchar(255) not null check (type in ('JELLYFIN_SYNC', 'CSV_IMPORT', 'RADARR_IMPORT', 'RADARR_RATINGS_REFRESH')),
    primary key (id)
);

INSERT INTO background_jobs_new SELECT * FROM background_jobs;

DROP TABLE background_jobs;

ALTER TABLE background_jobs_new RENAME TO background_jobs;
