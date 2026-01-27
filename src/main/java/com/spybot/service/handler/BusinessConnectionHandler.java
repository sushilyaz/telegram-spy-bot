package com.spybot.service.handler;

import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.business.BusinessConnection;
import com.spybot.repository.BusinessConnectionRepository;
import com.spybot.service.telegram.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class BusinessConnectionHandler {

    private final BusinessConnectionRepository connectionRepository;
    private final NotificationService notificationService;

    @Transactional
    public void handle(BusinessConnection connection) {
        String connectionId = connection.id();
        User user = connection.user();
        boolean isEnabled = connection.isEnabled();

        log.info("action=handle_business_connection, connection_id={}, user_id={}, enabled={}",
                connectionId, user.id(), isEnabled);

        com.spybot.domain.entity.BusinessConnection existing =
                connectionRepository.findByConnectionId(connectionId).orElse(null);

        if (existing != null) {
            existing.setIsEnabled(isEnabled);
            existing.setCanReply(connection.canReply());
            if (!isEnabled) {
                existing.setDisconnectedAt(Instant.now());
            }
            connectionRepository.save(existing);

            log.info("action=business_connection_updated, connection_id={}, user_id={}, enabled={}",
                    connectionId, user.id(), isEnabled);
        } else {
            com.spybot.domain.entity.BusinessConnection newConnection =
                    com.spybot.domain.entity.BusinessConnection.builder()
                            .connectionId(connectionId)
                            .userId(user.id())
                            .userChatId(connection.userChatId())
                            .username(user.username())
                            .firstName(user.firstName())
                            .lastName(user.lastName())
                            .canReply(connection.canReply())
                            .isEnabled(isEnabled)
                            .connectedAt(Instant.ofEpochSecond(connection.date()))
                            .build();

            connectionRepository.save(newConnection);

            log.info("action=business_connection_created, connection_id={}, user_id={}",
                    connectionId, user.id());
        }

        if (isEnabled) {
            notificationService.sendConnectionNotification(connection.userChatId(), true);
        } else {
            notificationService.sendConnectionNotification(connection.userChatId(), false);
        }
    }
}
