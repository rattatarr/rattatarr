package com.rattatarr.rattatarr.services.helpers;

import com.rattatarr.rattatarr.clients.tmdb.TMDbClient;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.MediaItemMetadata;
import org.hibernate.Hibernate;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

@Component
@NullMarked
public class MediaItemViewHelper {
    private final TMDbClient tmdbClient;

    public MediaItemViewHelper(TMDbClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }

    public void initializeCredits(MediaItem mediaItem, boolean shouldLoadCredits) {
        if (!shouldLoadCredits) {
            return;
        }
        Hibernate.initialize(mediaItem.cast());
        mediaItem.cast().forEach(cast -> Hibernate.initialize(cast.person()));
        Hibernate.initialize(mediaItem.crew());
        mediaItem.crew().forEach(crew -> Hibernate.initialize(crew.person()));
    }

    public void applyImageUrls(MediaItem mediaItem, String posterSize, String backdropSize, String profileSize, boolean includeSeasons, boolean includeCredits) {
        MediaItemMetadata metadata = mediaItem.metadata();
        if (metadata != null) {
            if (metadata.posterImageUrl() != null) {
                metadata.setPosterImageUrl(tmdbClient.getImageUrl(metadata.posterImageUrl(), posterSize));
            }
            if (metadata.backdropImageUrl() != null) {
                metadata.setBackdropImageUrl(tmdbClient.getImageUrl(metadata.backdropImageUrl(), backdropSize));
            }
        }

        if (includeSeasons) {
            mediaItem.seasons().forEach(season -> {
                var seasonMetadata = season.metadata();
                if (seasonMetadata != null && seasonMetadata.posterImageUrl() != null) {
                    seasonMetadata.setPosterImageUrl(tmdbClient.getImageUrl(seasonMetadata.posterImageUrl(), posterSize));
                }
            });
        }

        if (includeCredits) {
            mediaItem.cast().forEach(cast -> {
                var person = cast.person();
                if (person != null && person.profilePathUrl() != null) {
                    person.setProfilePathUrl(tmdbClient.getImageUrl(person.profilePathUrl(), profileSize));
                }
            });

            mediaItem.crew().forEach(crew -> {
                var person = crew.person();
                if (person != null && person.profilePathUrl() != null) {
                    person.setProfilePathUrl(tmdbClient.getImageUrl(person.profilePathUrl(), profileSize));
                }
            });
        }
    }

    public String buildUrlFromPath(
            String path,
            String size
    ) {
        return tmdbClient.getImageUrl(path, size);
    }
}
