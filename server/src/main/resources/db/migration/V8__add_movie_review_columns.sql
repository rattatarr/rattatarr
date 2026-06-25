-- Written reviews attached to a movie rating. A review is either FREE_TEXT (review_text)
-- or STRUCTURED (the six category columns). review_type is null when the rating has no review.
-- All review columns hold sanitized HTML (rich text rendered by the frontend).
ALTER TABLE media_item_ratings ADD COLUMN review_type varchar(255) check (review_type in ('FREE_TEXT', 'STRUCTURED'));
ALTER TABLE media_item_ratings ADD COLUMN review_text text;
ALTER TABLE media_item_ratings ADD COLUMN review_story text;
ALTER TABLE media_item_ratings ADD COLUMN review_performances text;
ALTER TABLE media_item_ratings ADD COLUMN review_direction text;
ALTER TABLE media_item_ratings ADD COLUMN review_visuals text;
ALTER TABLE media_item_ratings ADD COLUMN review_sound text;
ALTER TABLE media_item_ratings ADD COLUMN review_verdict text;
