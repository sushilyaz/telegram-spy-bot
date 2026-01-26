package com.spybot.service.telegram;

import com.pengrad.telegrambot.model.BusinessConnection;
import com.pengrad.telegrambot.model.BusinessMessagesDeleted;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.spybot.service.handler.BusinessConnectionHandler;
import com.spybot.service.handler.BusinessMessageHandler;
import com.spybot.service.handler.CommandHandler;
import com.spybot.service.handler.DeletedMessageHandler;
import com.spybot.service.handler.EditedMessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UpdateDispatcherService {

    private final BusinessConnectionHandler businessConnectionHandler;
    private final BusinessMessageHandler businessMessageHandler;
    private final EditedMessageHandler editedMessageHandler;
    private final DeletedMessageHandler deletedMessageHandler;
    private final CommandHandler commandHandler;

    @Async("telegramExecutor")
    public void dispatch(Update update) {
        log.debug("action=dispatch_update, update_id={}", update.updateId());

        try {
            BusinessConnection businessConnection = update.businessConnection();
            Message businessMessage = update.businessMessage();
            Message editedBusinessMessage = update.editedBusinessMessage();
            BusinessMessagesDeleted deletedBusinessMessages = update.deletedBusinessMessages();
            Message directMessage = update.message();

            if (businessConnection != null) {
                log.info("action=received_business_connection, connection_id={}",
                        businessConnection.id());
                businessConnectionHandler.handle(businessConnection);
            } else if (businessMessage != null) {
                log.debug("action=received_business_message, message_id={}, chat_id={}",
                        businessMessage.messageId(),
                        businessMessage.chat().id());
                businessMessageHandler.handle(businessMessage);
            } else if (editedBusinessMessage != null) {
                log.info("action=received_edited_business_message, message_id={}, chat_id={}",
                        editedBusinessMessage.messageId(),
                        editedBusinessMessage.chat().id());
                editedMessageHandler.handle(editedBusinessMessage);
            } else if (deletedBusinessMessages != null) {
                log.info("action=received_deleted_business_messages, chat_id={}, count={}",
                        deletedBusinessMessages.chat().id(),
                        deletedBusinessMessages.messageIds().length);
                deletedMessageHandler.handle(deletedBusinessMessages);
            } else if (directMessage != null) {
                handleDirectMessage(directMessage);
            }
        } catch (Exception e) {
            log.error("action=dispatch_failed, update_id={}, error={}",
                    update.updateId(), e.getMessage(), e);
        }
    }

    private void handleDirectMessage(Message message) {
        String text = message.text();
        if (text != null) {
            if ("/start".equals(text)) {
                commandHandler.handleStartCommand(message);
            } else if ("/help".equals(text)) {
                commandHandler.handleHelpCommand(message);
            }
        }
    }
}
