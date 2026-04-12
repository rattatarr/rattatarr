package com.rattatarr.rattatarr.clients;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientConnectionWarmer {
    private static final Logger logger = LoggerFactory.getLogger(ClientConnectionWarmer.class);

    private final List<Warmable> clients;

    public ClientConnectionWarmer(List<Warmable> clients) {
        this.clients = clients;
    }

    @PostConstruct
    public void warmConnections() {
        for (Warmable client : clients) {
            String name = client.getClass().getSimpleName();
            if (!client.isConfigured()) {
                logger.info("{} is not configured, skipping connection warm-up.", name);
                continue;
            }
            try {
                logger.info("Warming up {} connection...", name);
                client.warmUp();
                logger.info("{} connection warmed successfully.", name);
            } catch (Exception e) {
                logger.warn("{} connection warm-up failed: {}", name, e.getMessage());
            }
        }
    }
}
