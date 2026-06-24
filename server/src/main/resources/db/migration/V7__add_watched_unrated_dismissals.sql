-- Tombstones for items a profile removed from the "watched but not rated" list.
-- An item re-appears automatically once a qualifying watch event newer than
-- dismissed_at exists (see MediaItemSpecifications.recentlyWatchedUnrated).
CREATE TABLE IF NOT EXISTS watched_unrated_dismissals (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    dismissed_at timestamp not null,
    media_item_id blob not null,
    profile_id blob not null,
    primary key (id),
    constraint uq_dismissal_profile_item unique (profile_id, media_item_id)
);

CREATE INDEX IF NOT EXISTS idx_dismissal_profile_item ON watched_unrated_dismissals (profile_id, media_item_id);
