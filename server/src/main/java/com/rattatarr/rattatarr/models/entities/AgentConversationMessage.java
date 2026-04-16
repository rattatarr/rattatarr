package com.rattatarr.rattatarr.models.entities;

import com.rattatarr.rattatarr.clients.agents.AgentMessage;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "agent_conversation_messages",
        indexes = {
                @Index(name = "idx_agent_msg_profile_agent_sent", columnList = "profile_id, agent_name, sent_at")
        })
public class AgentConversationMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentMessage.Role role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "agent_name", nullable = false)
    private String agentName;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    protected AgentConversationMessage() {}

    public AgentConversationMessage(Profile profile, AgentMessage.Role role, String content, String agentName) {
        this.profile = profile;
        this.role = role;
        this.content = content;
        this.agentName = agentName;
        this.sentAt = Instant.now();
    }

    public Profile profile() {
        return profile;
    }

    public AgentMessage.Role role() {
        return role;
    }

    public String content() {
        return content;
    }

    public String agentName() {
        return agentName;
    }

    public Instant sentAt() {
        return sentAt;
    }
}
