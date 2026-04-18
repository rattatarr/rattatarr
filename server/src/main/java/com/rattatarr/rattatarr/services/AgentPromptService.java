package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.RatedItemSummary;
import com.rattatarr.rattatarr.models.WatchEventType;
import com.rattatarr.rattatarr.models.entities.MediaItem;
import com.rattatarr.rattatarr.models.entities.Profile;
import com.rattatarr.rattatarr.repositories.WatchEventsRepository;
import com.rattatarr.rattatarr.specifications.StatisticsSpecifications;
import jakarta.persistence.Tuple;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@NullMarked
public class AgentPromptService {

    private static final int RECENT_WATCHES_FALLBACK_THRESHOLD = 10;

    private static final String TOPIC_GUARD =
            "IMPORTANT: You are a movie and TV series assistant. You MUST only answer questions "
                    + "related to movies, TV shows, series, directors, actors, genres, or general entertainment. "
                    + "If the user asks about anything unrelated to movies or TV, politely decline and "
                    + "suggest they ask a movie/TV question instead. Keep answers concise.";

    private final MediaItemRatingsService mediaItemRatingsService;
    private final WatchEventsRepository watchEventsRepository;

    public AgentPromptService(
            MediaItemRatingsService mediaItemRatingsService,
            WatchEventsRepository watchEventsRepository) {
        this.mediaItemRatingsService = mediaItemRatingsService;
        this.watchEventsRepository = watchEventsRepository;
    }

    public String buildSystemPrompt(Profile profile, UUID profileId) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a movie and TV series recommendation assistant for the user named '")
                .append(profile.name())
                .append("'.\n\n");
        sb.append(TOPIC_GUARD).append("\n\n");
        appendRatingContext(sb, profile, profileId);
        return sb.toString();
    }

    // --- rating context ---

    private void appendRatingContext(StringBuilder sb, Profile profile, UUID profileId) {
        List<RatedItemSummary> allRecentFirst = mediaItemRatingsService.findAllRatedByProfileRecentFirst(profile);
        int total = allRecentFirst.size();

        if (total > 0) {
            appendWatchedExclusionList(sb, allRecentFirst, total);
        }

        if (total < RECENT_WATCHES_FALLBACK_THRESHOLD) {
            appendRecentWatchesContext(sb, profileId, allRecentFirst);
        }

        List<RatedItemSummary> topRated = mediaItemRatingsService.findTop15ByProfileOrderByRatingDesc(profile);
        if (!topRated.isEmpty()) {
            sb.append("User's top-rated titles (title, type, rating):\n");
            for (RatedItemSummary r : topRated) {
                sb.append("- ")
                        .append(r.title())
                        .append(" (")
                        .append(r.mediaType())
                        .append(") — ")
                        .append(r.rating())
                        .append("/10\n");
            }
            sb.append("\n");
        }

        appendGenreContext(sb, profileId, total);
        appendPersonContext(sb, profileId);
    }

    private void appendWatchedExclusionList(
            StringBuilder sb, List<RatedItemSummary> allRecentFirst, int total) {
        if (total <= 100) {
            sb.append("Titles the user has already watched and rated — do NOT recommend these:\n");
            allRecentFirst.forEach(r -> sb.append("- ").append(r.title()).append("\n"));
            sb.append("\n");
            return;
        }

        // Heavy watcher: list most recent 60 + top-rated 40 (deduped) and add a behavioural hint
        List<String> recent = allRecentFirst.stream()
                .limit(60)
                .map(RatedItemSummary::title)
                .toList();

        List<String> topRatedNotInRecent = allRecentFirst.stream()
                .sorted((a, b) -> Float.compare(b.rating(), a.rating()))
                .map(RatedItemSummary::title)
                .filter(title -> !recent.contains(title))
                .limit(40)
                .toList();

        sb.append("Titles the user has already watched and rated — do NOT recommend these:\n");
        recent.forEach(title -> sb.append("- ").append(title).append("\n"));
        topRatedNotInRecent.forEach(title -> sb.append("- ").append(title).append("\n"));
        sb.append("\n");

        sb.append("IMPORTANT: This user has rated ").append(total).append(" titles in total. ")
                .append("They are a heavy watcher who has likely already seen most mainstream and popular titles ")
                .append("in their favourite genres. Strongly prefer recommending: ")
                .append("niche, lesser-known, foreign-language, independent, upcoming or recent (last 2 years) releases. ")
                .append("Avoid obvious blockbusters unless specifically asked. Don't go into experimental movies.\n\n");
    }

    private void appendRecentWatchesContext(
            StringBuilder sb, UUID profileId, List<RatedItemSummary> alreadyRated) {
        Set<String> ratedTitles = alreadyRated.stream()
                .map(RatedItemSummary::title)
                .collect(Collectors.toSet());

        List<MediaItem> recentWatches = watchEventsRepository.findRecentWatchedMediaItems(
                profileId, WatchEventType.COMPLETE, PageRequest.of(0, 15));

        List<MediaItem> unrated = recentWatches.stream()
                .filter(m -> !ratedTitles.contains(m.title()))
                .limit(10)
                .toList();

        if (unrated.isEmpty()) {
            return;
        }

        sb.append("Titles the user recently watched but hasn't rated yet:\n");
        for (MediaItem m : unrated) {
            sb.append("- ").append(m.title()).append(" (").append(m.mediaType().name()).append(")\n");
        }
        sb.append("\n");
    }

    // --- genre context ---

    private void appendGenreContext(StringBuilder sb, UUID profileId, int totalRated) {
        if (totalRated < RECENT_WATCHES_FALLBACK_THRESHOLD) {
            List<Tuple> jellyfinGenres = mediaItemRatingsService.findJellyfinTopGenresByCount(profileId, 8);
            if (!jellyfinGenres.isEmpty()) {
                sb.append("Genres by number of titles watched (most to least):\n");
                for (Tuple t : jellyfinGenres) {
                    sb.append("- ").append(t.get("genreName")).append(": ").append(t.get("count")).append(" titles\n");
                }
                sb.append("\n");
            }
            return;
        }

        List<Tuple> byCount = mediaItemRatingsService.findTopGenresByWatchCount(profileId, 8);
        if (!byCount.isEmpty()) {
            sb.append("Genres by number of titles rated (most to least):\n");
            for (Tuple t : byCount) {
                sb.append("- ").append(t.get("genreName")).append(": ").append(t.get("count")).append(" titles\n");
            }
            sb.append("\n");
        }

        List<Tuple> byRating = mediaItemRatingsService.findTopGenresByAverageRating(profileId, 6.0f, 8);
        if (!byRating.isEmpty()) {
            sb.append("Genres by average rating (most liked):\n");
            for (Tuple t : byRating) {
                double avg = ((Number) t.get("averageRating")).doubleValue();
                sb.append("- ").append(t.get("genreName")).append(": ").append(String.format("%.1f", avg)).append("/10\n");
            }
            sb.append("\n");
        }
    }

    // --- person context ---

    private void appendPersonContext(StringBuilder sb, UUID profileId) {
        appendPersonSection(sb, mediaItemRatingsService.findTopActors(profileId, 2, 8, StatisticsSpecifications.SortBy.COUNT), "Favourite actors (by titles watched)");
        appendPersonSection(sb, mediaItemRatingsService.findTopActors(profileId, 3, 6, StatisticsSpecifications.SortBy.SCORE), "Favourite actors (by avg rating)");
        appendPersonSection(sb, mediaItemRatingsService.findTopDirectors(profileId, 2, 6, StatisticsSpecifications.SortBy.COUNT), "Favourite directors (by titles watched)");
        appendPersonSection(sb, mediaItemRatingsService.findTopDirectors(profileId, 3, 5, StatisticsSpecifications.SortBy.SCORE), "Favourite directors (by avg rating)");
        appendPersonSection(sb, mediaItemRatingsService.findTopProducers(profileId, 2, 5, StatisticsSpecifications.SortBy.COUNT), "Favourite producers (by titles watched)");
        appendPersonSection(sb, mediaItemRatingsService.findTopProducers(profileId, 3, 4, StatisticsSpecifications.SortBy.SCORE), "Favourite producers (by avg rating)");
    }

    private void appendPersonSection(StringBuilder sb, List<Tuple> tuples, String label) {
        if (tuples.isEmpty()) {
            return;
        }
        sb.append(label).append(":\n");
        for (Tuple t : tuples) {
            long count = ((Number) t.get("itemCount")).longValue();
            double avg = ((Number) t.get("averageRating")).doubleValue();
            sb.append("- ").append(t.get("name"))
                    .append(": ").append(count).append(" title").append(count == 1 ? "" : "s")
                    .append(", avg ").append(String.format("%.1f", avg)).append("/10\n");
        }
        sb.append("\n");
    }
}
