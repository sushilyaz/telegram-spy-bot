package com.spybot.service.handler;

import com.pengrad.telegrambot.model.Message;
import com.spybot.config.TelegramBotConfig;
import com.spybot.service.ReferralService;
import com.spybot.service.telegram.TelegramBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommandHandler {

    private final ReferralService referralService;
    private final TelegramBotConfig botConfig;

    private TelegramBotService botService;

    public void setBotService(TelegramBotService botService) {
        this.botService = botService;
    }

    public void handleStartCommand(Message message) {
        Long chatId = message.chat().id();
        Long userId = message.from().id();
        String userName = message.from().firstName();
        String text = message.text();

        // Обработка реферальной ссылки /start ref_XXXXXXXX
        if (text != null && text.startsWith("/start ref_")) {
            String refCode = text.substring(11).trim();
            referralService.processReferralStart(userId, refCode);
        } else {
            referralService.getOrCreateReferral(userId);
        }

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
                /premium — премиум функции
                /referral — твоя реф. ссылка
                """;

        botService.sendTextMessage(chatId, helpMessage);
        log.info("action=help_command_handled, user_id={}", message.from().id());
    }

    public void handlePremiumCommand(Message message) {
        Long chatId = message.chat().id();
        Long userId = message.from().id();

        boolean hasPremium = referralService.hasPremiumAccess(userId);
        int referralCount = referralService.getReferralCount(userId);
        int needed = referralService.getReferralsNeeded(userId);

        String premiumMessage;
        if (hasPremium) {
            premiumMessage = """
                    ⭐ <b>Премиум активен!</b>

                    Тебе доступны все функции:
                    • Сохранение одноразовых фото/видео

                    Спасибо за поддержку!
                    """;
        } else {
            premiumMessage = String.format("""
                    ⭐ <b>Премиум функции</b>

                    Разблокируй дополнительные возможности:
                    • Сохранение одноразовых фото/видео

                    ━━━━━━━━━━━━━━━━
                    <b>Как получить?</b>
                    ━━━━━━━━━━━━━━━━
                    Пригласи 3 друзей по реферальной ссылке.

                    📊 Прогресс: <b>%d/3</b>
                    Осталось пригласить: <b>%d</b>

                    /referral — получить ссылку
                    """, referralCount, needed);
        }

        botService.sendTextMessage(chatId, premiumMessage);
        log.info("action=premium_command_handled, user_id={}, has_premium={}", userId, hasPremium);
    }

    public void handleReferralCommand(Message message) {
        Long chatId = message.chat().id();
        Long userId = message.from().id();

        String refCode = referralService.getReferralCode(userId);
        if (refCode == null) {
            referralService.getOrCreateReferral(userId);
            refCode = referralService.getReferralCode(userId);
        }

        int referralCount = referralService.getReferralCount(userId);
        boolean hasPremium = referralService.hasPremiumAccess(userId);

        String botUsername = botConfig.getUsername();
        String referralLink = String.format("https://t.me/%s?start=ref_%s", botUsername, refCode);

        String statusText = hasPremium
                ? "✅ Премиум активен!"
                : String.format("📊 Приглашено: <b>%d/3</b>", referralCount);

        String referralMessage = String.format("""
                🔗 <b>Твоя реферальная ссылка</b>

                <code>%s</code>

                Отправь эту ссылку друзьям.
                После 3 приглашений откроется премиум.

                %s
                """, referralLink, statusText);

        botService.sendTextMessage(chatId, referralMessage);
        log.info("action=referral_command_handled, user_id={}", userId);
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
