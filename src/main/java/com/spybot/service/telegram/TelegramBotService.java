package com.spybot.service.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.LinkPreviewOptions;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendVideo;
import com.pengrad.telegrambot.request.SendVideoNote;
import com.pengrad.telegrambot.request.SendVoice;
import com.pengrad.telegrambot.request.SendSticker;
import com.pengrad.telegrambot.response.SendResponse;
import com.spybot.config.TelegramBotConfig;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Slf4j
public class TelegramBotService {

    private final TelegramBotConfig botConfig;
    private final TelegramBot bot;

    public TelegramBotService(TelegramBotConfig botConfig) {
        this.botConfig = botConfig;
        this.bot = new TelegramBot(botConfig.getToken());
        log.info("action=bot_created, username={}", botConfig.getUsername());
    }

    public void startListening(Consumer<Update> updateHandler) {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                try {
                    updateHandler.accept(update);
                } catch (Exception e) {
                    log.error("action=update_processing_failed, update_id={}, error={}",
                            update.updateId(), e.getMessage());
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, e -> {
            if (e.response() != null) {
                log.error("action=updates_listener_error, code={}, description={}",
                        e.response().errorCode(), e.response().description());
            } else {
                log.error("action=updates_listener_error, error={}", e.getMessage());
            }
        });

        log.info("action=bot_listening_started, username={}", botConfig.getUsername());
    }

    @PreDestroy
    public void shutdown() {
        if (bot != null) {
            bot.removeGetUpdatesListener();
            bot.shutdown();
            log.info("action=bot_shutdown");
        }
    }

    public void sendTextMessage(Long chatId, String text) {
        try {
            SendMessage request = new SendMessage(chatId, text)
                    .parseMode(ParseMode.HTML)
                    .linkPreviewOptions(new LinkPreviewOptions().isDisabled(true));

            SendResponse response = bot.execute(request);
            if (response.isOk()) {
                log.debug("action=message_sent, chat_id={}", chatId);
            } else {
                log.error("action=send_message_failed, chat_id={}, error_code={}, description={}",
                        chatId, response.errorCode(), response.description());
            }
        } catch (Exception e) {
            log.error("action=send_message_failed, chat_id={}, error={}", chatId, e.getMessage());
        }
    }

    public void sendPhoto(Long chatId, String fileId, String caption) {
        try {
            SendPhoto request = new SendPhoto(chatId, fileId)
                    .caption(caption)
                    .parseMode(ParseMode.HTML);

            SendResponse response = bot.execute(request);
            if (response.isOk()) {
                log.debug("action=photo_sent, chat_id={}", chatId);
            } else {
                log.error("action=send_photo_failed, chat_id={}, error={}", chatId, response.description());
            }
        } catch (Exception e) {
            log.error("action=send_photo_failed, chat_id={}, error={}", chatId, e.getMessage());
        }
    }

    public void sendVideo(Long chatId, String fileId, String caption) {
        try {
            SendVideo request = new SendVideo(chatId, fileId)
                    .caption(caption)
                    .parseMode(ParseMode.HTML);

            SendResponse response = bot.execute(request);
            if (response.isOk()) {
                log.debug("action=video_sent, chat_id={}", chatId);
            } else {
                log.error("action=send_video_failed, chat_id={}, error={}", chatId, response.description());
            }
        } catch (Exception e) {
            log.error("action=send_video_failed, chat_id={}, error={}", chatId, e.getMessage());
        }
    }

    public void sendDocument(Long chatId, String fileId, String caption) {
        try {
            SendDocument request = new SendDocument(chatId, fileId)
                    .caption(caption)
                    .parseMode(ParseMode.HTML);

            SendResponse response = bot.execute(request);
            if (response.isOk()) {
                log.debug("action=document_sent, chat_id={}", chatId);
            } else {
                log.error("action=send_document_failed, chat_id={}, error={}", chatId, response.description());
            }
        } catch (Exception e) {
            log.error("action=send_document_failed, chat_id={}, error={}", chatId, e.getMessage());
        }
    }

    public void sendVideoNote(Long chatId, String fileId) {
        try {
            SendVideoNote request = new SendVideoNote(chatId, fileId);

            SendResponse response = bot.execute(request);
            if (response.isOk()) {
                log.debug("action=video_note_sent, chat_id={}", chatId);
            } else {
                log.error("action=send_video_note_failed, chat_id={}, error={}", chatId, response.description());
            }
        } catch (Exception e) {
            log.error("action=send_video_note_failed, chat_id={}, error={}", chatId, e.getMessage());
        }
    }

    public void sendVoice(Long chatId, String fileId, String caption) {
        try {
            SendVoice request = new SendVoice(chatId, fileId)
                    .caption(caption)
                    .parseMode(ParseMode.HTML);

            SendResponse response = bot.execute(request);
            if (response.isOk()) {
                log.debug("action=voice_sent, chat_id={}", chatId);
            } else {
                log.error("action=send_voice_failed, chat_id={}, error={}", chatId, response.description());
            }
        } catch (Exception e) {
            log.error("action=send_voice_failed, chat_id={}, error={}", chatId, e.getMessage());
        }
    }

    public void sendSticker(Long chatId, String fileId) {
        try {
            SendSticker request = new SendSticker(chatId, fileId);

            SendResponse response = bot.execute(request);
            if (response.isOk()) {
                log.debug("action=sticker_sent, chat_id={}", chatId);
            } else {
                log.error("action=send_sticker_failed, chat_id={}, error={}", chatId, response.description());
            }
        } catch (Exception e) {
            log.error("action=send_sticker_failed, chat_id={}, error={}", chatId, e.getMessage());
        }
    }
}
