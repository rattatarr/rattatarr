package com.rattatarr.rattatarr.models.entities;

import com.rattatarr.rattatarr.models.MediaReviewType;

public interface ReviewableRating {
    MediaReviewType reviewType();

    String reviewText();

    String reviewStory();

    String reviewPerformances();

    String reviewDirection();

    String reviewVisuals();

    String reviewSound();

    String reviewVerdict();

    void setReviewType(MediaReviewType reviewType);

    void setReviewText(String reviewText);

    void setReviewStory(String reviewStory);

    void setReviewPerformances(String reviewPerformances);

    void setReviewDirection(String reviewDirection);

    void setReviewVisuals(String reviewVisuals);

    void setReviewSound(String reviewSound);

    void setReviewVerdict(String reviewVerdict);
}
