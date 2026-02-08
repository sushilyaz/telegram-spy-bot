package com.spybot.service.handler;

import com.pengrad.telegrambot.model.Message;
import com.spybot.service.telegram.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommandHandler {

    private TelegramBotService botService;

    public void setBotService(TelegramBotService botService) {
        this.botService = botService;
    }

    public void handleStartCommand(Message message) {
        Long chatId = message.chat().id();
        String userName = message.from().firstName();

        String welcomeMessage = String.format("""
                👋 <b>Привет, %s!</b>

                Я сохраняю удалённые и изменённые сообщения в твоих чатах.

                ━━━━━━━━━━━━━━━━
                <b>⚡ Требования</b>
                ━━━━━━━━━━━━━━━━
                • Telegram Premium
                • Telegram Business

                ━━━━━━━━━━━━━━━━
                <b>🔧 Подключение</b>
                ━━━━━━━━━━━━━━━━
                <b>Настройки</b> → <b>Telegram Business</b> → <b>Чатботы</b> → выбери меня → укажи чаты

                ━━━━━━━━━━━━━━━━
                <b>📬 Что отслеживаю</b>
                ━━━━━━━━━━━━━━━━
                • Текст, фото, видео, файлы
                • Голосовые, кружки, стикеры
                • Подписи к медиа

                ━━━━━━━━━━━━━━━━
                <b>🔔 Уведомления</b>
                ━━━━━━━━━━━━━━━━
                🗑 <b>Удаление</b> — получишь копию
                ✏️ <b>Изменение</b> — было/стало

                ━━━━━━━━━━━━━━━━
                <b>🔒 Безопасность</b>
                ━━━━━━━━━━━━━━━━
                AES-256 шифрование. Хранение 30 дней.

                /help — подробная справка
                """, escapeHtml(userName != null ? userName : "пользователь"));

        botService.sendTextMessage(chatId, welcomeMessage);
        log.info("action=start_command_handled, user_id={}", message.from().id());
    }

    public void handleHelpCommand(Message message) {
        Long chatId = message.chat().id();

        String helpMessage = """
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
                /help — эта справка
                """;

        botService.sendTextMessage(chatId, helpMessage);
        log.info("action=help_command_handled, user_id={}", message.from().id());
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
