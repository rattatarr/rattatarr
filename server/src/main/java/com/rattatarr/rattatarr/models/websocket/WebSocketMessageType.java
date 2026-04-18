package com.rattatarr.rattatarr.models.websocket;

public enum WebSocketMessageType {
    JOB_STARTED,
    JOB_PROGRESS,
    JOB_COMPLETED,
    JOB_FAILED,
    KEEP_ALIVE
}
