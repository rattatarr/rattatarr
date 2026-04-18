package com.rattatarr.rattatarr.repositories;

import com.rattatarr.rattatarr.models.entities.AgentConversationMessage;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AgentConversationMessageRepository extends BaseRepository<AgentConversationMessage> {

    @Query(
            "SELECT m FROM AgentConversationMessage m WHERE m.profile.id = :profileId AND m.agentName = :agentName ORDER BY m.sentAt DESC LIMIT :limit")
    List<AgentConversationMessage> findRecentByProfileAndAgent(
            @Param("profileId") UUID profileId,
            @Param("agentName") String agentName,
            @Param("limit") int limit);

    @Modifying
    @Query("DELETE FROM AgentConversationMessage m WHERE m.profile.id = :profileId AND m.agentName = :agentName")
    void deleteByProfileAndAgent(@Param("profileId") UUID profileId, @Param("agentName") String agentName);
}
