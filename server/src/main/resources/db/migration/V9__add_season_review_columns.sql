-- Per-season written reviews. Same shape as movie/whole-series reviews (see V8): either
-- FREE_TEXT (review_text) or STRUCTURED (the six category columns). review_type is null when
-- the season rating has no review. All review columns hold sanitized HTML.
ALTER TABLE media_season_ratings ADD COLUMN review_type varchar(255) check (review_type in ('FREE_TEXT', 'STRUCTURED'));
ALTER TABLE media_season_ratings ADD COLUMN review_text text;
ALTER TABLE media_season_ratings ADD COLUMN review_story text;
ALTER TABLE media_season_ratings ADD COLUMN review_performances text;
ALTER TABLE media_season_ratings ADD COLUMN review_direction text;
ALTER TABLE media_season_ratings ADD COLUMN review_visuals text;
ALTER TABLE media_season_ratings ADD COLUMN review_sound text;
ALTER TABLE media_season_ratings ADD COLUMN review_verdict text;
