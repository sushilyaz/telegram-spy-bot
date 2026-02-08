package com.spybot.service.handler;

import com.pengrad.telegrambot.model.business.BusinessMessageDeleted;
import com.spybot.domain.entity.MessageEvent;
import com.spybot.domain.entity.StoredMessage;
import com.spybot.domain.enums.EventType;
import com.spybot.repository.BusinessConnectionRepository;
import com.spybot.repository.MessageEventRepository;
import com.spybot.repository.StoredMessageRepository;
import com.spybot.service.telegram.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeletedMessageHandler {

    private final StoredMessageRepository messageRepository;
    private final MessageEventRepository eventRepository;
    private final BusinessConnectionRepository connectionRepository;
    private final NotificationService notificationService;

    @Transactional
    public void handle(BusinessMessageDeleted deleted) {
        String connectionId = deleted.businessConnectionId();
        Long chatId = deleted.chat().id();
        Integer[] messageIdsArray = deleted.messageIds();

        if (connectionId == null) {
            log.debug("action=skip_delete, reason=no_connection_id");
            return;
        }

        var connectionOpt = connectionRepository.findByConnectionId(connectionId);
        if (connectionOpt.isEmpty() || !connectionOpt.get().getIsEnabled()) {
            log.debug("action=skip_delete, reason=connection_disabled, connection_id={}", connectionId);
            return;
        }

        Long ownerId = connectionOpt.get().getUserId();

        List<Integer> messageIds = Arrays.asList(messageIdsArray);
        List<StoredMessage> storedMessages = messageRepository
                .findByChatIdAndMessageIdIn(chatId, messageIds);

        if (storedMessages.isEmpty()) {
            log.debug("action=delete_no_stored_messages, chat_id={}, message_ids={}",
                    chatId, messageIds);
            return;
        }

        for (StoredMessage storedMessage : storedMessages) {
            // Пропускаем свои собственные сообщения
            if (storedMessage.getFromUserId().equals(ownerId)) {
                log.debug("action=skip_delete_own_message, chat_id={}, message_id={}",
                        chatId, storedMessage.getMessageId());
                continue;
            }

            MessageEvent event = MessageEvent.builder()
                    .storedMessage(storedMessage)
                    .eventType(EventType.MESSAGE_DELETED)
                    .encryptedOldText(storedMessage.getEncryptedText())
                    .encryptedOldCaption(storedMessage.getEncryptedCaption())
                    .build();

            eventRepository.save(event);

            storedMessage.setIsDeleted(true);
            messageRepository.save(storedMessage);

            log.info("action=message_delete_recorded, chat_id={}, message_id={}",
                    chatId, storedMessage.getMessageId());

            notificationService.notifyMessageDeleted(connectionId, storedMessage);
        }
    }
}
