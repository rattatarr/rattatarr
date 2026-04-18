package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.entities.BackgroundJob;
import com.rattatarr.rattatarr.models.websocket.WebSocketMessageType;

public interface JobEventPublisher {
    void publish(BackgroundJob job, WebSocketMessageType messageType);
}
