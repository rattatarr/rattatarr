package com.rattatarr.rattatarr.controllers;

import com.rattatarr.rattatarr.models.websocket.WebSocketMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/keepalive")
    @SendTo("/topic/keepalive")
    public WebSocketMessage<Void> handleKeepAlive() {
        return WebSocketMessage.keepAlive();
    }
}
