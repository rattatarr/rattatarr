package com.rattatarr.rattatarr.clients.agents.ollama;

import com.rattatarr.rattatarr.clients.agents.AgentClient;
import com.rattatarr.rattatarr.clients.agents.AgentMessage;
import com.rattatarr.rattatarr.exceptions.AgentExceptions;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@NullMarked
public class OllamaAgentClient implements AgentClient {

    private static final String AGENT_NAME = "ollama";
    private static final Duration TIMEOUT = Duration.ofMinutes(10);

    private final Logger logger = LoggerFactory.getLogger(OllamaAgentClient.class);
    private final OllamaConfig config;

    public OllamaAgentClient(OllamaConfig config) {
        this.config = config;
    }

    @Override
    public String chat(String systemPrompt, List<AgentMessage> conversationHistory, String userMessage) {
        config.throwIfNotConfigured();

        OllamaChatModel model = buildModel();

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(systemPrompt));

        for (AgentMessage msg : conversationHistory) {
            if (msg.role() == AgentMessage.Role.USER) {
                messages.add(UserMessage.from(msg.content()));
            } else {
                messages.add(AiMessage.from(msg.content()));
            }
        }

        messages.add(UserMessage.from(userMessage));

        try {
            var response = model.chat(messages);
            return response.aiMessage().text();
        } catch (Exception e) {
            logger.error("Ollama chat failed: {}", e.getMessage());
            throw new AgentExceptions.AgentCallFailedException(AGENT_NAME, e);
        }
    }

    @Override
    public boolean isConfigured() {
        return config.isConfigured();
    }

    @Override
    public String name() {
        return AGENT_NAME;
    }

    private OllamaChatModel buildModel() {
        String baseUrl = config.getBaseUrl();
        String apiKey = config.getApiKey();
        String model = config.getModel();

        logger.debug("Building Ollama model: {} at {}", model, baseUrl);

        OllamaChatModel.OllamaChatModelBuilder builder = OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(model)
                .temperature(0.7)
                .timeout(TIMEOUT);

        if (StringUtils.hasText(apiKey)) {
            Map<String, String> customHeaders = new HashMap<>();
            customHeaders.put("Authorization", "Bearer " + apiKey);
            builder.customHeaders(customHeaders);
        }

        return builder.build();
    }
}
