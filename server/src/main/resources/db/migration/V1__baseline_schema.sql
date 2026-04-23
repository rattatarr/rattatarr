-- Baseline schema snapshot. Applied only to fresh databases.
-- Existing databases are marked as already at version 1 via baseline-on-migrate.

CREATE TABLE IF NOT EXISTS broken_media_items (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    imdb_id varchar(255) unique,
    tmdb_id varchar(255) unique,
    jellyfin_id varchar(255) not null unique,
    media_type varchar(255) not null check (media_type in ('MOVIE', 'SERIES')),
    missing_fields varchar(255) not null,
    production_year integer,
    resolved boolean not null,
    title varchar(255) not null,
    resolved_media_item_id blob unique,
    primary key (id)
);

CREATE TABLE IF NOT EXISTS genres (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    name varchar(255) not null unique,
    primary key (id)
);

CREATE TABLE IF NOT EXISTS media_completions (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    completed boolean not null,
    completed_at timestamp,
    days_to_complete integer,
    first_watched_at timestamp,
    target_id blob not null,
    target_type varchar(255) not null check (target_type in ('ITEM', 'SEASON', 'EPISODE')),
    profile_id blob not null,
    primary key (id)
);

CREATE TABLE IF NOT EXISTS media_episodes (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    episode integer not null,
    jellyfin_id varchar(255) unique,
    runtime_minutes integer,
    title varchar(255) not null,
    media_season_id blob not null,
    primary key (id)
);

CREATE TABLE IF NOT EXISTS media_item_cast (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    character varchar(255) not null,
    cast_order integer,
    media_item_id blob not null,
    person_id blob not null,
    primary key (id)
);

CREATE TABLE IF NOT EXISTS media_item_crew (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    department varchar(255) not null,
    job varchar(255) not null,
    media_item_id blob not null,
    person_id blob not null,
    primary key (id)
);

CREATE TABLE IF NOT EXISTS media_item_genres (
    media_item_id blob not null,
    genre_id blob not null,
    primary key (media_item_id, genre_id)
);

CREATE TABLE IF NOT EXISTS media_item_metadata (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    backdrop_image_url varchar(255),
    description TEXT,
    poster_image_url varchar(255),
    media_item_id blob not null unique,
    imdb_rating float,
    rotten_tomatoes_rating integer,
    primary key (id)
);

CREATE TABLE IF NOT EXISTS media_item_ratings (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    rated_at timestamp,
    rating float not null,
    media_item_id blob not null,
    profile_id blob not null,
    primary key (id)
);

CREATE TABLE IF NOT EXISTS media_items (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    imdb_id varchar(255) unique,
    tmdb_id varchar(255) unique,
    jellyfin_id varchar(255) unique,
    media_type varchar(255) not null check (media_type in ('MOVIE', 'SERIES')),
    production_year integer not null,
    runtime_minutes integer,
    title varchar(255) not null,
    primary key (id)
);

CREATE TABLE IF NOT EXISTS media_season_metadata (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    air_date varchar(255),
    episode_count integer,
    poster_image_url varchar(255),
    media_season_id blob not null unique,
    primary key (id)
);

CREATE TABLE IF NOT EXISTS media_season_ratings (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    rating float not null,
    media_season_id blob not null,
    profile_id blob not null,
    primary key (id)
);

CREATE TABLE IF NOT EXISTS media_seasons (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    jellyfin_id varchar(255) unique,
    season integer not null,
    title varchar(255),
    media_item_id blob not null,
    primary key (id)
);

CREATE TABLE IF NOT EXISTS people (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    tmdb_id varchar(255) not null unique,
    name varchar(255) not null,
    profile_path_url varchar(255),
    primary key (id)
);

CREATE TABLE IF NOT EXISTS profiles (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    jellyfin_id varchar(255) unique,
    name varchar(100) not null unique,
    primary key (id)
);

CREATE TABLE IF NOT EXISTS settings (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    description TEXT,
    setting_key varchar(100) not null unique,
    setting_value TEXT,
    primary key (id)
);

CREATE TABLE IF NOT EXISTS watch_events (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    event_type varchar(255) not null check (event_type in ('START', 'PROGRESS', 'COMPLETE')),
    jellyfin_log_id bigint,
    position_seconds integer,
    watched_at timestamp not null,
    episode_id blob,
    media_item_id blob not null,
    media_season_id blob,
    profile_id blob not null,
    primary key (id)
);

CREATE TABLE IF NOT EXISTS agent_conversation_messages (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    agent_name varchar(255) not null,
    content TEXT not null,
    role varchar(255) not null check (role in ('USER', 'ASSISTANT')),
    sent_at timestamp not null,
    profile_id blob not null,
    primary key (id)
);

CREATE TABLE IF NOT EXISTS background_jobs (
    id blob not null,
    created_at timestamp not null,
    deleted_at timestamp,
    updated_at timestamp not null,
    finished_at timestamp,
    message varchar(255),
    profile_id blob,
    started_at timestamp,
    status varchar(255) not null check (status in ('PENDING', 'RUNNING', 'COMPLETED', 'FAILED')),
    type varchar(255) not null check (type in ('JELLYFIN_SYNC', 'CSV_IMPORT')),
    primary key (id)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_episode_media_season_id ON media_episodes (media_season_id);
CREATE INDEX IF NOT EXISTS idx_cast_media_item_id ON media_item_cast (media_item_id);
CREATE INDEX IF NOT EXISTS idx_cast_person_id ON media_item_cast (person_id);
CREATE INDEX IF NOT EXISTS idx_crew_media_item_id ON media_item_crew (media_item_id);
CREATE INDEX IF NOT EXISTS idx_crew_person_id ON media_item_crew (person_id);
CREATE INDEX IF NOT EXISTS idx_crew_job ON media_item_crew (job);
CREATE INDEX IF NOT EXISTS idx_item_genre_genre_id ON media_item_genres (genre_id);
CREATE INDEX IF NOT EXISTS idx_item_rating_media_item_id ON media_item_ratings (media_item_id);
CREATE INDEX IF NOT EXISTS idx_season_rating_media_season_id ON media_season_ratings (media_season_id);
CREATE INDEX IF NOT EXISTS idx_season_media_item_id ON media_seasons (media_item_id);
CREATE INDEX IF NOT EXISTS idx_watch_profile_time ON watch_events (profile_id, watched_at);
CREATE INDEX IF NOT EXISTS idx_watch_media_time ON watch_events (media_item_id);
CREATE INDEX IF NOT EXISTS idx_watch_season_time ON watch_events (media_season_id);
CREATE INDEX IF NOT EXISTS idx_agent_msg_profile_agent_sent ON agent_conversation_messages (profile_id, agent_name, sent_at);
