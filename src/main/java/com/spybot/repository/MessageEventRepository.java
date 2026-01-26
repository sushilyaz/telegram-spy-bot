package com.spybot.repository;

import com.spybot.domain.entity.MessageEvent;
import com.spybot.domain.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface MessageEventRepository extends JpaRepository<MessageEvent, Long> {

    List<MessageEvent> findByStoredMessageIdOrderByEventTimeDesc(Long storedMessageId);

    List<MessageEvent> findByUserNotifiedFalseOrderByEventTimeAsc();

    @Query("SELECT me FROM MessageEvent me JOIN FETCH me.storedMessage sm " +
            "WHERE sm.businessConnectionId = :connectionId AND me.userNotified = false " +
            "ORDER BY me.eventTime ASC")
    List<MessageEvent> findUnnotifiedEventsByConnectionId(@Param("connectionId") String connectionId);

    long countByEventType(EventType eventType);

    @Modifying
    @Query("UPDATE MessageEvent me SET me.userNotified = true, me.notifiedAt = :now WHERE me.id = :eventId")
    int markAsNotified(@Param("eventId") Long eventId, @Param("now") Instant now);

    @Modifying
    @Query("DELETE FROM MessageEvent me WHERE me.eventTime < :cutoffDate")
    int deleteEventsOlderThan(@Param("cutoffDate") Instant cutoffDate);
}
