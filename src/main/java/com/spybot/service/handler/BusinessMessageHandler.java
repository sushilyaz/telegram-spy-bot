package com.spybot.service.handler;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.User;
import com.spybot.domain.entity.StoredMessage;
import com.spybot.domain.enums.MediaType;
import com.spybot.repository.BusinessConnectionRepository;
import com.spybot.service.MessageStorageService;
import com.spybot.service.encryption.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;

@Service
@Slf4j
@RequiredArgsConstructor
public class BusinessMessageHandler {

    private final MessageStorageService messageStorageService;
    private final BusinessConnectionRepository connectionRepository;
    private final EncryptionService encryptionService;

    @Transactional
    public void handle(Message message) {
        String connectionId = message.businessConnectionId();

        if (connectionId == null) {
            log.debug("action=skip_message, reason=no_connection_id");
            return;
        }

        if (!connectionRepository.existsByConnectionIdAndIsEnabledTrue(connectionId)) {
            log.debug("action=skip_message, reason=connection_disabled, connection_id={}", connectionId);
            return;
        }

        User fromUser = message.from();
        MediaType mediaType = determineMediaType(message);
        String fileId = extractFileId(message, mediaType);

        String encryptedText = encryptionService.encrypt(message.text());
        String encryptedCaption = encryptionService.encrypt(message.caption());

        StoredMessage storedMessage = StoredMessage.builder()
                .businessConnectionId(connectionId)
                .chatId(message.chat().id())
                .messageId(message.messageId())
                .fromUserId(fromUser.id())
                .fromUsername(fromUser.username())
                .fromFirstName(fromUser.firstName())
                .fromLastName(fromUser.lastName())
                .encryptedText(encryptedText)
                .mediaType(mediaType)
                .mediaFileId(fileId)
                .encryptedCaption(encryptedCaption)
                .messageDate(Instant.ofEpochSecond(message.date()))
                .build();

        messageStorageService.storeMessage(storedMessage);

        log.debug("action=message_stored, connection_id={}, chat_id={}, message_id={}, media_type={}",
                connectionId, message.chat().id(), message.messageId(), mediaType);
    }

    private MediaType determineMediaType(Message message) {
        if (message.photo() != null && message.photo().length > 0) return MediaType.PHOTO;
        if (message.video() != null) return MediaType.VIDEO;
        if (message.document() != null) return MediaType.DOCUMENT;
        if (message.voice() != null) return MediaType.VOICE;
        if (message.videoNote() != null) return MediaType.VIDEO_NOTE;
        if (message.audio() != null) return MediaType.AUDIO;
        if (message.sticker() != null) return MediaType.STICKER;
        if (message.animation() != null) return MediaType.ANIMATION;
        return MediaType.NONE;
    }

    private String extractFileId(Message message, MediaType mediaType) {
        return switch (mediaType) {
            case PHOTO -> {
                PhotoSize[] photos = message.photo();
                if (photos != null && photos.length > 0) {
                    yield Arrays.stream(photos)
                            .max(Comparator.comparingInt(PhotoSize::fileSize))
                            .map(PhotoSize::fileId)
                            .orElse(null);
                }
                yield null;
            }
            case VIDEO -> message.video().fileId();
            case DOCUMENT -> message.document().fileId();
            case VOICE -> message.voice().fileId();
            case VIDEO_NOTE -> message.videoNote().fileId();
            case AUDIO -> message.audio().fileId();
            case STICKER -> message.sticker().fileId();
            case ANIMATION -> message.animation().fileId();
            case NONE -> null;
        };
    }
}
