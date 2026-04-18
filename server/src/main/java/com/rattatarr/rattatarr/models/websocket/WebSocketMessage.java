package com.rattatarr.rattatarr.models.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WebSocketMessage<T>(
        WebSocketMessageType message_type,
        T payload
) {
    public static <T> WebSocketMessage<T> of(WebSocketMessageType type, T payload) {
        return new WebSocketMessage<>(type, payload);
    }

    public static WebSocketMessage<Void> keepAlive() {
        return new WebSocketMessage<>(WebSocketMessageType.KEEP_ALIVE, null);
    }
}
