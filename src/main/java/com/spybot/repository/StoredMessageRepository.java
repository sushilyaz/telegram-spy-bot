package com.spybot.repository;

import com.spybot.domain.entity.StoredMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoredMessageRepository extends JpaRepository<StoredMessage, Long> {

    Optional<StoredMessage> findByChatIdAndMessageId(Long chatId, Integer messageId);

    boolean existsByChatIdAndMessageId(Long chatId, Integer messageId);

    List<StoredMessage> findByChatIdAndMessageIdIn(Long chatId, List<Integer> messageIds);

    @Query("SELECT sm FROM StoredMessage sm WHERE sm.businessConnectionId = :connectionId AND sm.isDeleted = false")
    List<StoredMessage> findActiveMessagesByConnectionId(@Param("connectionId") String connectionId);

    @Modifying
    @Query("UPDATE StoredMessage sm SET sm.isDeleted = true WHERE sm.chatId = :chatId AND sm.messageId IN :messageIds")
    int markMessagesAsDeleted(@Param("chatId") Long chatId, @Param("messageIds") List<Integer> messageIds);

    @Modifying
    @Query("DELETE FROM StoredMessage sm WHERE sm.storedAt < :cutoffDate")
    int deleteMessagesOlderThan(@Param("cutoffDate") Instant cutoffDate);
}
