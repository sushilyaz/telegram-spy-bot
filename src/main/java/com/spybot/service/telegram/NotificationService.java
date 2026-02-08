package com.spybot.service.telegram;

import com.spybot.domain.entity.BusinessConnection;
import com.spybot.domain.entity.StoredMessage;
import com.spybot.domain.enums.MediaType;
import com.spybot.repository.BusinessConnectionRepository;
import com.spybot.service.encryption.EncryptionService;
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

    private TelegramBotService botService;

    public void setBotService(TelegramBotService botService) {
        this.botService = botService;
    }

    @Async("notificationExecutor")
    public void sendConnectionNotification(Long chatId, boolean connected) {
        String message = connected
                ? "✅ <b>Бот подключен</b>\n\nТеперь я буду отслеживать изменения и удаления сообщений в выбранных чатах."
                : "❌ <b>Бот отключен</b>\n\nОтслеживание сообщений прекращено.";

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

        String senderName = formatSenderName(storedMessage);
        String decryptedText = encryptionService.decrypt(storedMessage.getEncryptedText());
        String decryptedCaption = encryptionService.decrypt(storedMessage.getEncryptedCaption());

        StringBuilder notification = new StringBuilder();
        notification.append("🗑 <b>Сообщение удалено</b>\n\n");
        notification.append("👤 <b>От:</b> ").append(escapeHtml(senderName)).append("\n");

        if (decryptedText != null && !decryptedText.isEmpty()) {
            notification.append("📝 <b>Текст:</b>\n").append(escapeHtml(truncateText(decryptedText))).append("\n");
        }

        if (storedMessage.getMediaType() != MediaType.NONE) {
            notification.append("📎 <b>Тип медиа:</b> ").append(getMediaTypeName(storedMessage.getMediaType())).append("\n");

            if (decryptedCaption != null && !decryptedCaption.isEmpty()) {
                notification.append("💬 <b>Подпись:</b> ").append(escapeHtml(truncateText(decryptedCaption))).append("\n");
            }
        }

        botService.sendTextMessage(connection.getUserChatId(), notification.toString());

        if (storedMessage.getMediaFileId() != null && storedMessage.getMediaType() != MediaType.NONE) {
            sendMediaNotification(connection.getUserChatId(), storedMessage, "🗑 Удалённое медиа от " + senderName);
        }

        log.info("action=delete_notification_sent, connection_id={}, user_chat_id={}",
                connectionId, connection.getUserChatId());
    }

    @Async("notificationExecutor")
    public void notifyViewOnceSaved(String connectionId, StoredMessage storedMessage) {
        BusinessConnection connection = connectionRepository.findByConnectionId(connectionId).orElse(null);
        if (connection == null) {
            return;
        }

        String senderName = formatSenderName(storedMessage);

        String notification = String.format("""
                🔥 <b>Одноразовое сообщение сохранено!</b>

                👤 <b>От:</b> %s
                📎 <b>Тип:</b> %s
                """, escapeHtml(senderName), getMediaTypeName(storedMessage.getMediaType()));

        botService.sendTextMessage(connection.getUserChatId(), notification);

        // Одноразовые медиа нужно скачать и отправить заново
        if (storedMessage.getMediaFileId() != null) {
            sendViewOnceMedia(connection.getUserChatId(), storedMessage, senderName);
        }

        log.info("action=view_once_notification_sent, connection_id={}", connectionId);
    }

    private void sendViewOnceMedia(Long chatId, StoredMessage message, String senderName) {
        String fileId = message.getMediaFileId();
        byte[] fileData = botService.downloadFile(fileId);

        if (fileData == null) {
            log.error("action=view_once_download_failed, file_id={}", fileId);
            botService.sendTextMessage(chatId, "⚠️ Не удалось скачать медиа");
            return;
        }

        String caption = "🔥 Одноразовое медиа от " + senderName;

        switch (message.getMediaType()) {
            case PHOTO -> botService.sendPhotoBytes(chatId, fileData, caption);
            case VIDEO -> botService.sendVideoBytes(chatId, fileData, caption);
            case VIDEO_NOTE -> botService.sendVideoNoteBytes(chatId, fileData);
            case ANIMATION -> botService.sendAnimationBytes(chatId, fileData, caption);
            default -> botService.sendDocumentBytes(chatId, fileData, caption, "media");
        }
    }

    public void sendPremiumRequiredNotification(Long chatId) {
        String message = """
                ⭐ <b>Премиум функция</b>

                Сохранение одноразовых сообщений доступно после приглашения 3 друзей.

                /premium — подробнее
                /referral — получить ссылку
                """;

        botService.sendTextMessage(chatId, message);
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

        String senderName = formatSenderName(storedMessage);

        StringBuilder notification = new StringBuilder();
        notification.append("✏️ <b>Сообщение изменено</b>\n\n");
        notification.append("👤 <b>От:</b> ").append(escapeHtml(senderName)).append("\n\n");

        boolean hasTextChange = !equalsNullSafe(oldText, newText);
        boolean hasCaptionChange = !equalsNullSafe(oldCaption, newCaption);

        if (hasTextChange) {
            notification.append("📝 <b>Было:</b>\n").append(escapeHtml(truncateText(oldText != null ? oldText : "(пусто)"))).append("\n\n");
            notification.append("📝 <b>Стало:</b>\n").append(escapeHtml(truncateText(newText != null ? newText : "(пусто)"))).append("\n");
        }

        if (hasCaptionChange) {
            notification.append("\n💬 <b>Подпись была:</b>\n").append(escapeHtml(truncateText(oldCaption != null ? oldCaption : "(пусто)"))).append("\n\n");
            notification.append("💬 <b>Подпись стала:</b>\n").append(escapeHtml(truncateText(newCaption != null ? newCaption : "(пусто)"))).append("\n");
        }

        if (storedMessage.getMediaType() != MediaType.NONE) {
            notification.append("\n📎 <b>Тип медиа:</b> ").append(getMediaTypeName(storedMessage.getMediaType()));
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

    private String getMediaTypeName(MediaType type) {
        return switch (type) {
            case PHOTO -> "Фото";
            case VIDEO -> "Видео";
            case DOCUMENT -> "Документ";
            case VOICE -> "Голосовое сообщение";
            case VIDEO_NOTE -> "Видеосообщение";
            case AUDIO -> "Аудио";
            case STICKER -> "Стикер";
            case ANIMATION -> "GIF";
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
