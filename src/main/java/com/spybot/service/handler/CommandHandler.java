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

                Я бот для отслеживания удалённых и изменённых сообщений.

                <b>Как подключить:</b>
                1. Откройте <b>Настройки</b> в Telegram
                2. Перейдите в <b>Telegram Business</b> → <b>Чатботы</b>
                3. Найдите меня и подключите
                4. Выберите чаты для мониторинга

                После подключения я буду уведомлять вас, когда кто-то удалит или изменит сообщение в выбранных чатах.

                /help — показать справку
                """, escapeHtml(userName != null ? userName : "пользователь"));

        botService.sendTextMessage(chatId, welcomeMessage);
        log.info("action=start_command_handled, user_id={}", message.from().id());
    }

    public void handleHelpCommand(Message message) {
        Long chatId = message.chat().id();

        String helpMessage = """
                📖 <b>Справка</b>

                <b>Что я умею:</b>
                • Отслеживать удалённые сообщения
                • Отслеживать изменённые сообщения
                • Сохранять фото, видео и документы

                <b>Типы уведомлений:</b>
                🗑 — сообщение было удалено
                ✏️ — сообщение было изменено

                <b>Важно:</b>
                • Мониторинг работает только в чатах, которые вы выбрали в настройках Telegram Business
                • Сообщения хранятся 30 дней, затем автоматически удаляются
                • Ваши данные зашифрованы и недоступны администраторам

                <b>Требования:</b>
                • Telegram Premium
                • Включенный Telegram Business
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
