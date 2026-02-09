package com.spybot.service.telegram;

import com.spybot.domain.entity.BusinessConnection;
import com.spybot.domain.entity.StoredMessage;
import com.spybot.domain.enums.MediaType;
import com.spybot.repository.BusinessConnectionRepository;
import com.spybot.service.encryption.EncryptionService;
import com.spybot.service.i18n.I18nService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final BusinessConnectionRepository connectionRepository;
    private final EncryptionService encryptionService;
    private final I18nService messages;

    private TelegramBotService botService;

    public void setBotService(TelegramBotService botService) {
        this.botService = botService;
    }

    @Async("notificationExecutor")
    public void sendConnectionNotification(Long chatId, boolean connected, String langCode) {
        String message = connected
                ? messages.get("connection.enabled", langCode)
                : messages.get("connection.disabled", langCode);

        botService.sendTextMessage(chatId, message);
        log.info("action=connection_notification_sent, chat_id={}, connected={}", chatId, connected);
    }

    @Async("notificationExecutor")
    public void notifyMessageDeleted(String connectionId, StoredMessage storedMessage) {
        BusinessConnection connection = connectionRepository.findByConnectionId(connectionId).orElse(null);
        if (connection == null) {
            log.warn("action=notify_deleted_failed, reason=connection_not_found, connection_id={}", connectionId);
            return;
        }

        // Default to Russian for now (can be extended to store user language preference)
        String langCode = "ru";

        String senderName = formatSenderName(storedMessage);
        String decryptedText = encryptionService.decrypt(storedMessage.getEncryptedText());
        String decryptedCaption = encryptionService.decrypt(storedMessage.getEncryptedCaption());

        StringBuilder notification = new StringBuilder();
        notification.append(messages.get("notify.deleted", langCode)).append("\n\n");
        notification.append(messages.get("notify.from", langCode)).append(" ").append(escapeHtml(senderName)).append("\n");

        if (decryptedText != null && !decryptedText.isEmpty()) {
            notification.append(messages.get("notify.text", langCode)).append("\n")
                    .append(escapeHtml(truncateText(decryptedText))).append("\n");
        }

        if (storedMessage.getMediaType() != MediaType.NONE) {
            notification.append(messages.get("notify.media_type", langCode)).append(" ")
                    .append(getMediaTypeName(storedMessage.getMediaType(), langCode)).append("\n");

            if (decryptedCaption != null && !decryptedCaption.isEmpty()) {
                notification.append(messages.get("notify.caption", langCode)).append(" ")
                        .append(escapeHtml(truncateText(decryptedCaption))).append("\n");
            }
        }

        botService.sendTextMessage(connection.getUserChatId(), notification.toString());

        if (storedMessage.getMediaFileId() != null && storedMessage.getMediaType() != MediaType.NONE) {
            String mediaCaption = messages.get("notify.deleted_media", langCode, senderName);
            sendMediaNotification(connection.getUserChatId(), storedMessage, mediaCaption);
        }

        log.info("action=delete_notification_sent, connection_id={}, user_chat_id={}",
                connectionId, connection.getUserChatId());
    }

    @Async("notificationExecutor")
    public void notifyMessageEdited(String connectionId, StoredMessage storedMessage,
                                    String oldText, String newText,
                                    String oldCaption, String newCaption) {
        BusinessConnection connection = connectionRepository.findByConnectionId(connectionId).orElse(null);
        if (connection == null) {
            log.warn("action=notify_edited_failed, reason=connection_not_found, connection_id={}", connectionId);
            return;
        }

        String langCode = "ru";
        String senderName = formatSenderName(storedMessage);
        String emptyText = messages.get("notify.empty", langCode);

        StringBuilder notification = new StringBuilder();
        notification.append(messages.get("notify.edited", langCode)).append("\n\n");
        notification.append(messages.get("notify.from", langCode)).append(" ").append(escapeHtml(senderName)).append("\n\n");

        boolean hasTextChange = !equalsNullSafe(oldText, newText);
        boolean hasCaptionChange = !equalsNullSafe(oldCaption, newCaption);

        if (hasTextChange) {
            notification.append(messages.get("notify.was", langCode)).append("\n")
                    .append(escapeHtml(truncateText(oldText != null ? oldText : emptyText))).append("\n\n");
            notification.append(messages.get("notify.became", langCode)).append("\n")
                    .append(escapeHtml(truncateText(newText != null ? newText : emptyText))).append("\n");
        }

        if (hasCaptionChange) {
            notification.append("\n").append(messages.get("notify.caption_was", langCode)).append("\n")
                    .append(escapeHtml(truncateText(oldCaption != null ? oldCaption : emptyText))).append("\n\n");
            notification.append(messages.get("notify.caption_became", langCode)).append("\n")
                    .append(escapeHtml(truncateText(newCaption != null ? newCaption : emptyText))).append("\n");
        }

        if (storedMessage.getMediaType() != MediaType.NONE) {
            notification.append("\n").append(messages.get("notify.media_type", langCode)).append(" ")
                    .append(getMediaTypeName(storedMessage.getMediaType(), langCode));
        }

        botService.sendTextMessage(connection.getUserChatId(), notification.toString());

        log.info("action=edit_notification_sent, connection_id={}, user_chat_id={}",
                connectionId, connection.getUserChatId());
    }

    private void sendMediaNotification(Long chatId, StoredMessage message, String caption) {
        String fileId = message.getMediaFileId();
        if (fileId == null) return;

        switch (message.getMediaType()) {
            case PHOTO -> botService.sendPhoto(chatId, fileId, caption);
            case VIDEO, ANIMATION -> botService.sendVideo(chatId, fileId, caption);
            case VIDEO_NOTE -> botService.sendVideoNote(chatId, fileId);
            case VOICE -> botService.sendVoice(chatId, fileId, caption);
            case STICKER -> botService.sendSticker(chatId, fileId);
            case DOCUMENT, AUDIO -> botService.sendDocument(chatId, fileId, caption);
            default -> {}
        }
    }

    private String formatSenderName(StoredMessage message) {
        StringBuilder name = new StringBuilder();
        if (message.getFromFirstName() != null) {
            name.append(message.getFromFirstName());
        }
        if (message.getFromLastName() != null) {
            if (!name.isEmpty()) name.append(" ");
            name.append(message.getFromLastName());
        }
        if (message.getFromUsername() != null) {
            if (!name.isEmpty()) name.append(" ");
            name.append("(@").append(message.getFromUsername()).append(")");
        }
        return name.isEmpty() ? "Unknown" : name.toString();
    }

    private String getMediaTypeName(MediaType type, String langCode) {
        return switch (type) {
            case PHOTO -> messages.get("media.photo", langCode);
            case VIDEO -> messages.get("media.video", langCode);
            case DOCUMENT -> messages.get("media.document", langCode);
            case VOICE -> messages.get("media.voice", langCode);
            case VIDEO_NOTE -> messages.get("media.video_note", langCode);
            case AUDIO -> messages.get("media.audio", langCode);
            case STICKER -> messages.get("media.sticker", langCode);
            case ANIMATION -> messages.get("media.animation", langCode);
            case NONE -> "";
        };
    }

    private String truncateText(String text) {
        if (text == null) return "";
        return text.length() > 1000 ? text.substring(0, 1000) + "..." : text;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private boolean equalsNullSafe(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
