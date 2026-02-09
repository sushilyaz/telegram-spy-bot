package com.spybot.service.i18n;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class MessageSource {

    private static final Set<String> RUSSIAN_CODES = Set.of("ru", "uk", "be", "kk", "uz");

    public boolean isRussian(String languageCode) {
        if (languageCode == null) return false;
        return RUSSIAN_CODES.contains(languageCode.toLowerCase());
    }

    public String get(String key, String languageCode) {
        Map<String, String> messages = isRussian(languageCode) ? RU : EN;
        return messages.getOrDefault(key, key);
    }

    public String get(String key, String languageCode, Object... args) {
        String template = get(key, languageCode);
        return String.format(template, args);
    }

    // ==================== RUSSIAN ====================
    private static final Map<String, String> RU = Map.ofEntries(
            Map.entry("start.greeting", """
                    👋 <b>Привет, %s!</b>

                    Я сохраняю удалённые и изменённые сообщения в твоих личных чатах.

                    ━━━━━━━━━━━━━━━━
                    <b>📬 Что я умею</b>
                    ━━━━━━━━━━━━━━━━
                    🗑 <b>Удалённые сообщения</b> — пришлю копию
                    ✏️ <b>Изменённые сообщения</b> — покажу было/стало

                    ━━━━━━━━━━━━━━━━
                    <b>🔧 Как подключить</b>
                    ━━━━━━━━━━━━━━━━
                    1. Нужен <b>Telegram Premium</b>
                    2. Открой <b>Настройки</b> → <b>Telegram Business</b>
                    3. Выбери <b>Чатботы</b> → найди меня
                    4. Укажи чаты для отслеживания
                    5. Готово! Я пришлю подтверждение.

                    ━━━━━━━━━━━━━━━━
                    <b>🔒 Безопасность</b>
                    ━━━━━━━━━━━━━━━━
                    • AES-256 шифрование всех сообщений
                    • Автоудаление через 30 дней
                    • Собеседник не узнает о боте

                    /help — справка и FAQ"""),

            Map.entry("help.message", """
                    📖 <b>Полная справка</b>

                    ━━━━━━━━━━━━━━━━
                    <b>⚠️ Ограничения</b>
                    ━━━━━━━━━━━━━━━━
                    • Только личные чаты
                    • Только выбранные чаты
                    • Хранение <b>30 дней</b>

                    ━━━━━━━━━━━━━━━━
                    <b>❓ FAQ</b>
                    ━━━━━━━━━━━━━━━━
                    <b>Q:</b> Не приходят уведомления?
                    <b>A:</b> Проверь подключение в Telegram Business.

                    <b>Q:</b> Собеседник узнает?
                    <b>A:</b> Нет, бот работает скрыто.

                    <b>Q:</b> Старые сообщения?
                    <b>A:</b> Только после подключения бота.

                    ━━━━━━━━━━━━━━━━
                    <b>🛠 Команды</b>
                    ━━━━━━━━━━━━━━━━
                    /start — начало работы
                    /help — эта справка"""),

            // Notifications
            Map.entry("connection.enabled", "✅ <b>Бот подключен</b>\n\nТеперь я буду отслеживать изменения и удаления сообщений в выбранных чатах."),
            Map.entry("connection.disabled", "❌ <b>Бот отключен</b>\n\nОтслеживание сообщений прекращено."),

            Map.entry("notify.deleted", "🗑 <b>Сообщение удалено</b>"),
            Map.entry("notify.edited", "✏️ <b>Сообщение изменено</b>"),

            Map.entry("notify.from", "👤 <b>От:</b>"),
            Map.entry("notify.text", "📝 <b>Текст:</b>"),
            Map.entry("notify.media_type", "📎 <b>Тип медиа:</b>"),
            Map.entry("notify.caption", "💬 <b>Подпись:</b>"),
            Map.entry("notify.was", "📝 <b>Было:</b>"),
            Map.entry("notify.became", "📝 <b>Стало:</b>"),
            Map.entry("notify.caption_was", "💬 <b>Подпись была:</b>"),
            Map.entry("notify.caption_became", "💬 <b>Подпись стала:</b>"),
            Map.entry("notify.deleted_media", "🗑 Удалённое медиа от %s"),
            Map.entry("notify.empty", "(пусто)"),

            // Media types
            Map.entry("media.photo", "Фото"),
            Map.entry("media.video", "Видео"),
            Map.entry("media.document", "Документ"),
            Map.entry("media.voice", "Голосовое сообщение"),
            Map.entry("media.video_note", "Видеосообщение"),
            Map.entry("media.audio", "Аудио"),
            Map.entry("media.sticker", "Стикер"),
            Map.entry("media.animation", "GIF")
    );

    // ==================== ENGLISH ====================
    private static final Map<String, String> EN = Map.ofEntries(
            Map.entry("start.greeting", """
                    👋 <b>Hi, %s!</b>

                    I save deleted and edited messages in your private chats.

                    ━━━━━━━━━━━━━━━━
                    <b>📬 What I can do</b>
                    ━━━━━━━━━━━━━━━━
                    🗑 <b>Deleted messages</b> — I'll send you a copy
                    ✏️ <b>Edited messages</b> — I'll show before/after

                    ━━━━━━━━━━━━━━━━
                    <b>🔧 How to connect</b>
                    ━━━━━━━━━━━━━━━━
                    1. You need <b>Telegram Premium</b>
                    2. Open <b>Settings</b> → <b>Telegram Business</b>
                    3. Select <b>Chatbots</b> → find me
                    4. Choose chats to monitor
                    5. Done! I'll send a confirmation.

                    ━━━━━━━━━━━━━━━━
                    <b>🔒 Security</b>
                    ━━━━━━━━━━━━━━━━
                    • AES-256 encryption for all messages
                    • Auto-delete after 30 days
                    • Your contacts won't know about the bot

                    /help — help and FAQ"""),

            Map.entry("help.message", """
                    📖 <b>Full guide</b>

                    ━━━━━━━━━━━━━━━━
                    <b>⚠️ Limitations</b>
                    ━━━━━━━━━━━━━━━━
                    • Private chats only
                    • Selected chats only
                    • Storage for <b>30 days</b>

                    ━━━━━━━━━━━━━━━━
                    <b>❓ FAQ</b>
                    ━━━━━━━━━━━━━━━━
                    <b>Q:</b> Not receiving notifications?
                    <b>A:</b> Check your Telegram Business connection.

                    <b>Q:</b> Will my contacts know?
                    <b>A:</b> No, the bot works invisibly.

                    <b>Q:</b> Old messages?
                    <b>A:</b> Only after connecting the bot.

                    ━━━━━━━━━━━━━━━━
                    <b>🛠 Commands</b>
                    ━━━━━━━━━━━━━━━━
                    /start — get started
                    /help — this guide"""),

            // Notifications
            Map.entry("connection.enabled", "✅ <b>Bot connected</b>\n\nI will now track edits and deletions in your selected chats."),
            Map.entry("connection.disabled", "❌ <b>Bot disconnected</b>\n\nMessage tracking stopped."),

            Map.entry("notify.deleted", "🗑 <b>Message deleted</b>"),
            Map.entry("notify.edited", "✏️ <b>Message edited</b>"),

            Map.entry("notify.from", "👤 <b>From:</b>"),
            Map.entry("notify.text", "📝 <b>Text:</b>"),
            Map.entry("notify.media_type", "📎 <b>Media type:</b>"),
            Map.entry("notify.caption", "💬 <b>Caption:</b>"),
            Map.entry("notify.was", "📝 <b>Was:</b>"),
            Map.entry("notify.became", "📝 <b>Became:</b>"),
            Map.entry("notify.caption_was", "💬 <b>Caption was:</b>"),
            Map.entry("notify.caption_became", "💬 <b>Caption became:</b>"),
            Map.entry("notify.deleted_media", "🗑 Deleted media from %s"),
            Map.entry("notify.empty", "(empty)"),

            // Media types
            Map.entry("media.photo", "Photo"),
            Map.entry("media.video", "Video"),
            Map.entry("media.document", "Document"),
            Map.entry("media.voice", "Voice message"),
            Map.entry("media.video_note", "Video message"),
            Map.entry("media.audio", "Audio"),
            Map.entry("media.sticker", "Sticker"),
            Map.entry("media.animation", "GIF")
    );
}
