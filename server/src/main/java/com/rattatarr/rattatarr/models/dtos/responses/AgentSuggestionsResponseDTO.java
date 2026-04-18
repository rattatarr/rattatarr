package com.rattatarr.rattatarr.models.dtos.responses;

import java.io.Serializable;
import java.util.List;

public record AgentSuggestionsResponseDTO(List<String> suggestions) implements Serializable {

    public static AgentSuggestionsResponseDTO defaults() {
        return new AgentSuggestionsResponseDTO(List.of(
                "What movies should I watch based on my highest-rated films?",
                "What are some underrated gems similar to my favorite genres?",
                "Can you recommend a TV series I'd likely enjoy given my ratings?",
                "Which directors from my watched list should I explore more?",
                "What's a good movie to watch tonight based on my taste?",
                "Are there any sequels or related films to titles I've rated highly?",
                "What are some critically acclaimed movies I might have missed?",
                "Can you suggest a movie for a movie night with friends based on my preferences?",
                "What films from the last year match my taste profile?",
                "Which of my highly rated movies share a common theme I might enjoy exploring?"));
    }
}
