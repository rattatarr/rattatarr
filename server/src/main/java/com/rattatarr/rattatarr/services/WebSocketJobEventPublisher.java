package com.rattatarr.rattatarr.services;

import com.rattatarr.rattatarr.models.dtos.responses.BackgroundJobResponseDTO;
import com.rattatarr.rattatarr.models.entities.BackgroundJob;
import com.rattatarr.rattatarr.models.websocket.WebSocketMessage;
import com.rattatarr.rattatarr.models.websocket.WebSocketMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class WebSocketJobEventPublisher implements JobEventPublisher {

    private static final String JOBS_TOPIC = "/topic/jobs/";

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketJobEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publish(BackgroundJob job, WebSocketMessageType messageType) {
        var message = WebSocketMessage.of(messageType, BackgroundJobResponseDTO.fromEntity(job));
        messagingTemplate.convertAndSend(JOBS_TOPIC + job.id(), message);
    }
}
