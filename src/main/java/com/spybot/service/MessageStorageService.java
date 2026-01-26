package com.spybot.service;

import com.spybot.domain.entity.StoredMessage;
import com.spybot.repository.MessageEventRepository;
import com.spybot.repository.StoredMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageStorageService {

    private final StoredMessageRepository messageRepository;
    private final MessageEventRepository eventRepository;

    @Value("${app.retention.days:30}")
    private int retentionDays;

    @Transactional
    public StoredMessage storeMessage(StoredMessage message) {
        return messageRepository.save(message);
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupOldMessages() {
        Instant cutoffDate = Instant.now().minus(retentionDays, ChronoUnit.DAYS);

        int deletedEvents = eventRepository.deleteEventsOlderThan(cutoffDate);
        int deletedMessages = messageRepository.deleteMessagesOlderThan(cutoffDate);

        log.info("action=cleanup_completed, deleted_events={}, deleted_messages={}, retention_days={}",
                deletedEvents, deletedMessages, retentionDays);
    }
}
