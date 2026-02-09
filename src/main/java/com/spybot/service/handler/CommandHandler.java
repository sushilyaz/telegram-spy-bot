package com.spybot.service.handler;

import com.pengrad.telegrambot.model.Message;
import com.spybot.service.i18n.MessageSource;
import com.spybot.service.telegram.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommandHandler {

    private final MessageSource messages;

    private TelegramBotService botService;

    public void setBotService(TelegramBotService botService) {
        this.botService = botService;
    }

    public void handleStartCommand(Message message) {
        Long chatId = message.chat().id();
        Long userId = message.from().id();
        String userName = message.from().firstName();
        String langCode = resolveLanguage(message);

        String displayName = escapeHtml(userName != null ? userName :
                (messages.isRussian(langCode) ? "пользователь" : "user"));
        String welcomeMessage = messages.get("start.greeting", langCode, displayName);

        botService.sendTextMessage(chatId, welcomeMessage);
        log.info("action=start_command_handled, user_id={}, lang={}", userId, langCode);
    }

    public void handleHelpCommand(Message message) {
        Long chatId = message.chat().id();
        Long userId = message.from().id();
        String langCode = resolveLanguage(message);

        String helpMessage = messages.get("help.message", langCode);
        botService.sendTextMessage(chatId, helpMessage);
        log.info("action=help_command_handled, user_id={}", userId);
    }

    private String resolveLanguage(Message message) {
        String telegramLang = message.from().languageCode();
        return telegramLang != null ? telegramLang : "en";
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
