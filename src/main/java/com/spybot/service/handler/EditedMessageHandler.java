package com.spybot.service.handler;

import com.pengrad.telegrambot.model.Message;
import com.spybot.domain.entity.MessageEvent;
import com.spybot.domain.entity.StoredMessage;
import com.spybot.domain.enums.EventType;
import com.spybot.repository.BusinessConnectionRepository;
import com.spybot.repository.MessageEventRepository;
import com.spybot.repository.StoredMessageRepository;
import com.spybot.service.encryption.EncryptionService;
import com.spybot.service.telegram.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EditedMessageHandler {

    private final StoredMessageRepository messageRepository;
    private final MessageEventRepository eventRepository;
    private final BusinessConnectionRepository connectionRepository;
    private final EncryptionService encryptionService;
    private final NotificationService notificationService;

    @Transactional
    public void handle(Message editedMessage) {
        String connectionId = editedMessage.businessConnectionId();

        if (connectionId == null || connectionId.isBlank()) {
            log.debug("action=skip_edit, reason=no_connection_id");
            return;
        }

        Long chatId = editedMessage.chat().id();
        Integer messageId = editedMessage.messageId();

        var connectionOpt = connectionRepository.findByConnectionId(connectionId);
        if (connectionOpt.isEmpty() || !connectionOpt.get().getIsEnabled()) {
            log.debug("action=skip_edit, reason=connection_disabled, connection_id={}", connectionId);
            return;
        }

        Long ownerId = connectionOpt.get().getUserId();

        StoredMessage storedMessage = messageRepository
                .findByChatIdAndMessageId(chatId, messageId)
                .orElse(null);

        if (storedMessage == null) {
            log.warn("action=edit_original_not_found, chat_id={}, message_id={}", chatId, messageId);
            return;
        }

        // Пропускаем свои собственные сообщения
        if (storedMessage.getFromUserId().equals(ownerId)) {
            log.debug("action=skip_edit_own_message, chat_id={}, message_id={}", chatId, messageId);
            return;
        }

        String oldText = encryptionService.decrypt(storedMessage.getEncryptedText());
        String newText = editedMessage.text();

        String oldCaption = encryptionService.decrypt(storedMessage.getEncryptedCaption());
        String newCaption = editedMessage.caption();

        MessageEvent event = MessageEvent.builder()
                .storedMessage(storedMessage)
                .eventType(EventType.MESSAGE_EDITED)
                .encryptedOldText(storedMessage.getEncryptedText())
                .encryptedNewText(encryptionService.encrypt(newText))
                .encryptedOldCaption(storedMessage.getEncryptedCaption())
                .encryptedNewCaption(encryptionService.encrypt(newCaption))
                .build();

        eventRepository.save(event);

        storedMessage.setEncryptedText(encryptionService.encrypt(newText));
        storedMessage.setEncryptedCaption(encryptionService.encrypt(newCaption));
        storedMessage.setEditCount(storedMessage.getEditCount() + 1);
        messageRepository.save(storedMessage);

        log.info("action=message_edit_recorded, chat_id={}, message_id={}, edit_count={}",
                chatId, messageId, storedMessage.getEditCount());

        notificationService.notifyMessageEdited(
                connectionId,
                storedMessage,
                oldText,
                newText,
                oldCaption,
                newCaption
        );
    }
}
