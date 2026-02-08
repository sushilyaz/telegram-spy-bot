package com.spybot.service.handler;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.User;
import com.spybot.domain.entity.BusinessConnection;
import com.spybot.domain.entity.StoredMessage;
import com.spybot.domain.enums.MediaType;
import com.spybot.repository.BusinessConnectionRepository;
import com.spybot.repository.StoredMessageRepository;
import com.spybot.service.ReferralService;
import com.spybot.service.encryption.EncryptionService;
import com.spybot.service.telegram.NotificationService;
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
public class ViewOnceMessageHandler {

    private final StoredMessageRepository messageRepository;
    private final BusinessConnectionRepository connectionRepository;
    private final EncryptionService encryptionService;
    private final ReferralService referralService;
    private final NotificationService notificationService;

    @Transactional
    public boolean tryHandleViewOnceReply(Message message) {
        log.debug("action=view_once_check_start, message_id={}, chat_id={}",
                message.messageId(), message.chat().id());

        Message replyTo = message.replyToMessage();
        if (replyTo == null) {
            log.debug("action=view_once_no_reply, message_id={}", message.messageId());
            return false;
        }

        log.info("action=view_once_has_reply, message_id={}, reply_to_id={}, " +
                "reply_has_photo={}, reply_has_video={}, reply_has_video_note={}, " +
                "reply_has_media_spoiler={}, reply_text={}",
                message.messageId(),
                replyTo.messageId(),
                replyTo.photo() != null,
                replyTo.video() != null,
                replyTo.videoNote() != null,
                replyTo.hasMediaSpoiler(),
                replyTo.text() != null ? replyTo.text().substring(0, Math.min(50, replyTo.text().length())) : null);

        // Проверяем, есть ли медиа в replyTo (одноразовые — это фото/видео)
        boolean hasMedia = replyTo.photo() != null || replyTo.video() != null ||
                           replyTo.videoNote() != null || replyTo.animation() != null;

        if (!hasMedia) {
            log.debug("action=view_once_no_media_in_reply, reply_to_id={}", replyTo.messageId());
            return false;
        }

        // hasMediaSpoiler может быть null для одноразовых, проверяем просто наличие медиа
        Boolean hasMediaSpoiler = replyTo.hasMediaSpoiler();
        log.info("action=view_once_media_check, reply_to_id={}, has_media_spoiler={}",
                replyTo.messageId(), hasMediaSpoiler);

        String connectionId = message.businessConnectionId();
        if (connectionId == null || connectionId.isBlank()) {
            log.debug("action=view_once_no_connection, message_id={}", message.messageId());
            return false;
        }

        // Получаем владельца бота
        var connectionOpt = connectionRepository.findByConnectionId(connectionId);
        if (connectionOpt.isEmpty() || !connectionOpt.get().getIsEnabled()) {
            return false;
        }

        BusinessConnection connection = connectionOpt.get();
        Long ownerId = connection.getUserId();

        log.info("action=view_once_connection_found, connection_id={}, owner_id={}", connectionId, ownerId);

        // Проверяем премиум доступ
        boolean hasPremium = referralService.hasPremiumAccess(ownerId);
        log.info("action=view_once_premium_check, owner_id={}, has_premium={}", ownerId, hasPremium);

        if (!hasPremium) {
            log.info("action=view_once_no_premium, user_id={}", ownerId);
            notificationService.sendPremiumRequiredNotification(connection.getUserChatId());
            return true; // Обработали, но без сохранения
        }

        // Проверяем, не сохранено ли уже это сообщение
        Long chatId = replyTo.chat() != null ? replyTo.chat().id() : message.chat().id();
        Integer messageId = replyTo.messageId();

        log.info("action=view_once_saving, chat_id={}, message_id={}", chatId, messageId);

        if (messageRepository.existsByChatIdAndMessageId(chatId, messageId)) {
            log.debug("action=skip_view_once, reason=already_stored, message_id={}", messageId);
            return true;
        }

        // Сохраняем одноразовое сообщение
        User fromUser = replyTo.from();
        MediaType mediaType = determineMediaType(replyTo);
        String fileId = extractFileId(replyTo, mediaType);

        if (fileId == null) {
            log.warn("action=view_once_no_file_id, message_id={}", messageId);
            return false;
        }

        StoredMessage storedMessage = StoredMessage.builder()
                .businessConnectionId(connectionId)
                .chatId(chatId)
                .messageId(messageId)
                .fromUserId(fromUser != null ? fromUser.id() : 0L)
                .fromUsername(fromUser != null ? fromUser.username() : null)
                .fromFirstName(fromUser != null ? fromUser.firstName() : null)
                .fromLastName(fromUser != null ? fromUser.lastName() : null)
                .encryptedText(null)
                .mediaType(mediaType)
                .mediaFileId(fileId)
                .encryptedCaption(encryptionService.encrypt(replyTo.caption()))
                .messageDate(Instant.ofEpochSecond(replyTo.date()))
                .build();

        messageRepository.save(storedMessage);

        log.info("action=view_once_saved, chat_id={}, message_id={}, media_type={}",
                chatId, messageId, mediaType);

        // Отправляем уведомление владельцу
        notificationService.notifyViewOnceSaved(connectionId, storedMessage);

        return true;
    }

    private MediaType determineMediaType(Message message) {
        if (message.photo() != null && message.photo().length > 0) return MediaType.PHOTO;
        if (message.video() != null) return MediaType.VIDEO;
        if (message.videoNote() != null) return MediaType.VIDEO_NOTE;
        if (message.animation() != null) return MediaType.ANIMATION;
        return MediaType.NONE;
    }

    private String extractFileId(Message message, MediaType mediaType) {
        return switch (mediaType) {
            case PHOTO -> {
                PhotoSize[] photos = message.photo();
                if (photos != null && photos.length > 0) {
                    yield Arrays.stream(photos)
                            .filter(p -> p.fileSize() != null)
                            .max(Comparator.comparingLong(PhotoSize::fileSize))
                            .map(PhotoSize::fileId)
                            .orElse(photos[photos.length - 1].fileId());
                }
                yield null;
            }
            case VIDEO -> message.video() != null ? message.video().fileId() : null;
            case VIDEO_NOTE -> message.videoNote() != null ? message.videoNote().fileId() : null;
            case ANIMATION -> message.animation() != null ? message.animation().fileId() : null;
            default -> null;
        };
    }
}
