package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.configs.ApiVersion;
import com.rattatarr.rattatarr.models.dtos.requests.AgentChatRequestDTO;
import com.rattatarr.rattatarr.models.dtos.responses.AgentChatResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.AgentConversationMessageResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.AgentPromptResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.AgentSuggestionsResponseDTO;
import com.rattatarr.rattatarr.models.dtos.responses.wrappers.AgentConversationWrapper;
import com.rattatarr.rattatarr.services.AgentConversationService;
import com.rattatarr.rattatarr.services.ProfileAgentService;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/agent")
@ApiVersion("v1")
@NullMarked
public class AgentController extends BaseController {

    private final ProfileAgentService profileAgentService;
    private final AgentConversationService conversationService;

    public AgentController(ProfileAgentService profileAgentService, AgentConversationService conversationService) {
        this.profileAgentService = profileAgentService;
        this.conversationService = conversationService;
    }

    @GetMapping("/suggestions")
    public ResponseEntity<AgentSuggestionsResponseDTO> getSuggestions() {
        return ResponseEntity.ok(AgentSuggestionsResponseDTO.defaults());
    }

    @PostMapping("/chat")
    public ResponseEntity<AgentChatResponseDTO> chat(
            @Valid @RequestBody AgentChatRequestDTO requestDTO,
            @RequestParam(defaultValue = "ollama") String agent) {
        logger.debug("Chat request for profile {} via agent {}", requestDTO.profileId(), agent);
        String reply = profileAgentService.chat(requestDTO.profileId(), agent, requestDTO.message());
        return ResponseEntity.ok(new AgentChatResponseDTO(reply, agent));
    }

    @GetMapping("/prompt/{profileId}")
    public ResponseEntity<AgentPromptResponseDTO> getPrompt(
            @PathVariable UUID profileId,
            @RequestParam(defaultValue = "ollama") String agent) {
        logger.debug("Building context prompt for profile {} agent {}", profileId, agent);
        String prompt = profileAgentService.buildContextPrompt(profileId);
        return ResponseEntity.ok(new AgentPromptResponseDTO(prompt, agent));
    }

    @GetMapping("/conversation/{profileId}")
    public ResponseEntity<AgentConversationWrapper> getConversation(
            @PathVariable UUID profileId,
            @RequestParam(defaultValue = "ollama") String agent) {
        logger.debug("Fetching conversation for profile {} agent {}", profileId, agent);
        List<AgentConversationMessageResponseDTO> messages = conversationService
                .fetchHistory(profileId, agent)
                .stream()
                .map(AgentConversationMessageResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(AgentConversationWrapper.of(messages));
    }

    @DeleteMapping("/conversation/{profileId}")
    public ResponseEntity<Void> clearConversation(
            @PathVariable UUID profileId,
            @RequestParam(defaultValue = "ollama") String agent) {
        logger.debug("Clearing conversation for profile {} agent {}", profileId, agent);
        profileAgentService.clearConversation(profileId, agent);
        return ResponseEntity.noContent().build();
    }
}
