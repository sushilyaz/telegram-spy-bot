package com.spybot.config;

import com.spybot.service.handler.CommandHandler;
import com.spybot.service.telegram.NotificationService;
import com.spybot.service.telegram.TelegramBotService;
import com.spybot.service.telegram.UpdateDispatcherService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class BotInitializer {

    private final TelegramBotService botService;
    private final UpdateDispatcherService updateDispatcher;
    private final NotificationService notificationService;
    private final CommandHandler commandHandler;

    @PostConstruct
    public void init() {
        // Wire bot service to components that need it (breaks circular dependency)
        notificationService.setBotService(botService);
        commandHandler.setBotService(botService);

        // Start listening for updates
        botService.startListening(updateDispatcher::dispatch);

        log.info("action=bot_initialized, status=success");
    }
}
