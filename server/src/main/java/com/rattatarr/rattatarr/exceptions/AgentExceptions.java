package com.rattatarr.rattatarr.exceptions;

import org.springframework.http.HttpStatus;

public class AgentExceptions extends BaseRattatarrExceptions {
    private AgentExceptions(String message, HttpStatus status) {
        super(message, status);
    }

    private AgentExceptions(String message, Throwable cause, HttpStatus status) {
        super(message, cause, status);
    }

    public static class AgentNotConfiguredException extends AgentExceptions {
        public AgentNotConfiguredException(String agentName) {
            super(
                    String.format(
                            "Agent '%s' is not configured. Please set the required settings (base URL, model).",
                            agentName),
                    HttpStatus.BAD_REQUEST);
        }
    }

    public static class AgentNotFoundException extends AgentExceptions {
        public AgentNotFoundException(String agentName) {
            super(
                    String.format("Unknown agent '%s'. Available agents: ollama", agentName),
                    HttpStatus.BAD_REQUEST);
        }
    }

    public static class AgentCallFailedException extends AgentExceptions {
        public AgentCallFailedException(String agentName, Throwable cause) {
            super(
                    String.format("Agent '%s' failed to respond: %s", agentName, cause.getMessage()),
                    cause,
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
