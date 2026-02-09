package com.spybot.service.i18n;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class I18nService {

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
    private static final Map<String, String> RU = new HashMap<>();
    static {
        RU.put("start.greeting", """
                👋 <b>Привет, %s!</b>

                Я сохраняю удалённые и изменённые сообщения в твоих личных чатах.

                <b>📬 Что я умею</b>
                🗑 <b>Удалённые сообщения</b> — пришлю копию
                ✏️ <b>Изменённые сообщения</b> — покажу было/стало

                <b>🔧 Как подключить</b>
                1️⃣ Нужен <b>Telegram Premium</b>
                2️⃣ <b>Настройки</b> → <b>Telegram Business</b> → <b>Чат-боты</b>
                3️⃣ Найди меня и подключи
                4️⃣ Выбери чаты для отслеживания
                5️⃣ Готово! Я пришлю подтверждение ✅

                <b>🔒 Безопасность</b>
                • <b>Open Source</b> — <a href="https://github.com/sushilyaz/telegram-spy-bot">GitHub</a>
                • AES-256 шифрование
                • Автоудаление через 30 дней
                • Собеседник не узнает о боте

                /help — справка""");

        RU.put("help.message", """
                📖 <b>Справка</b>

                <b>❓ FAQ</b>
                <b>Q: Как подключить бота?</b>
                A: Настройки → Telegram Business → Чат-боты

                <b>Q: Собеседник узнает о боте?</b>
                A: Нет, бот работает полностью скрыто.

                <b>Q: Почему не приходят уведомления?</b>
                A: Проверь подключение в Telegram Business.

                <b>⚠️ Ограничения</b>
                • Только личные чаты
                • Только выбранные чаты
                • Хранение 30 дней

                <b>📬 Связь</b>
                Вопросы и предложения: @suhoio

                <b>🛠 Команды</b>
                /start — начало работы
                /help — эта справка""");

        RU.put("connection.enabled", """
                ✅ <b>Бот подключен!</b>

                Теперь я отслеживаю изменения и удаления сообщений в выбранных чатах.

                /help — справка""");

        RU.put("connection.disabled", "❌ <b>Бот отключен</b>\n\nОтслеживание сообщений прекращено.");

        RU.put("notify.deleted", "🗑 <b>Сообщение удалено</b>");
        RU.put("notify.edited", "✏️ <b>Сообщение изменено</b>");

        RU.put("notify.from", "👤 <b>От:</b>");
        RU.put("notify.text", "📝 <b>Текст:</b>");
        RU.put("notify.media_type", "📎 <b>Тип медиа:</b>");
        RU.put("notify.caption", "💬 <b>Подпись:</b>");
        RU.put("notify.was", "📝 <b>Было:</b>");
        RU.put("notify.became", "📝 <b>Стало:</b>");
        RU.put("notify.caption_was", "💬 <b>Подпись была:</b>");
        RU.put("notify.caption_became", "💬 <b>Подпись стала:</b>");
        RU.put("notify.deleted_media", "🗑 Удалённое медиа от %s");
        RU.put("notify.empty", "(пусто)");

        RU.put("media.photo", "Фото");
        RU.put("media.video", "Видео");
        RU.put("media.document", "Документ");
        RU.put("media.voice", "Голосовое сообщение");
        RU.put("media.video_note", "Видеосообщение");
        RU.put("media.audio", "Аудио");
        RU.put("media.sticker", "Стикер");
        RU.put("media.animation", "GIF");
    }

    // ==================== ENGLISH ====================
    private static final Map<String, String> EN = new HashMap<>();
    static {
        EN.put("start.greeting", """
                👋 <b>Hi, %s!</b>

                I save deleted and edited messages in your private chats.

                <b>📬 What I can do</b>
                🗑 <b>Deleted messages</b> — I'll send you a copy
                ✏️ <b>Edited messages</b> — I'll show before/after

                <b>🔧 How to connect</b>
                1️⃣ You need <b>Telegram Premium</b>
                2️⃣ <b>Settings</b> → <b>Telegram Business</b> → <b>Chatbots</b>
                3️⃣ Find me and connect
                4️⃣ Choose chats to monitor
                5️⃣ Done! I'll send a confirmation ✅

                <b>🔒 Security</b>
                • <b>Open Source</b> — <a href="https://github.com/sushilyaz/telegram-spy-bot">GitHub</a>
                • AES-256 encryption
                • Auto-delete after 30 days
                • Your contacts won't know about the bot

                /help — help""");

        EN.put("help.message", """
                📖 <b>Help</b>

                <b>❓ FAQ</b>
                <b>Q: How to connect the bot?</b>
                A: Settings → Telegram Business → Chatbots

                <b>Q: Will my contacts know about the bot?</b>
                A: No, the bot works completely invisibly.

                <b>Q: Why am I not receiving notifications?</b>
                A: Check your connection in Telegram Business.

                <b>⚠️ Limitations</b>
                • Private chats only
                • Selected chats only
                • 30 days storage

                <b>📬 Contact</b>
                Questions and suggestions: @suhoio

                <b>🛠 Commands</b>
                /start — get started
                /help — this guide""");

        EN.put("connection.enabled", """
                ✅ <b>Bot connected!</b>

                I will now track edits and deletions in your selected chats.

                /help — help""");

        EN.put("connection.disabled", "❌ <b>Bot disconnected</b>\n\nMessage tracking stopped.");

        EN.put("notify.deleted", "🗑 <b>Message deleted</b>");
        EN.put("notify.edited", "✏️ <b>Message edited</b>");

        EN.put("notify.from", "👤 <b>From:</b>");
        EN.put("notify.text", "📝 <b>Text:</b>");
        EN.put("notify.media_type", "📎 <b>Media type:</b>");
        EN.put("notify.caption", "💬 <b>Caption:</b>");
        EN.put("notify.was", "📝 <b>Was:</b>");
        EN.put("notify.became", "📝 <b>Became:</b>");
        EN.put("notify.caption_was", "💬 <b>Caption was:</b>");
        EN.put("notify.caption_became", "💬 <b>Caption became:</b>");
        EN.put("notify.deleted_media", "🗑 Deleted media from %s");
        EN.put("notify.empty", "(empty)");

        EN.put("media.photo", "Photo");
        EN.put("media.video", "Video");
        EN.put("media.document", "Document");
        EN.put("media.voice", "Voice message");
        EN.put("media.video_note", "Video message");
        EN.put("media.audio", "Audio");
        EN.put("media.sticker", "Sticker");
        EN.put("media.animation", "GIF");
    }
}
