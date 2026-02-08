# Telegram Spy Bot

Бот для отслеживания удалённых и изменённых сообщений в Telegram через Telegram Business API.

## Как это работает

1. Пользователь с Telegram Premium подключает бота через "Telegram для бизнеса"
2. Бот получает копии всех сообщений в выбранных чатах
3. При удалении или изменении сообщения — пользователь получает уведомление

## Требования

- Java 21+
- PostgreSQL 16+
- Docker (для БД)
- Telegram Bot Token (от @BotFather)

## Безопасность

- **AES-256-GCM** шифрование всех текстов сообщений
- В логах НЕ сохраняется содержимое сообщений
- Данные автоматически удаляются через 30 дней

## Быстрый старт

### 1. Создание бота

1. Напишите @BotFather в Telegram
2. Создайте бота: `/newbot`
3. Сохраните токен
4. Включите Business Mode: `/mybots` → Ваш бот → Bot Settings → Business Mode → Enable

### 2. Генерация ключа шифрования

```bash
# Linux/macOS
openssl rand -base64 32

# Windows (PowerShell)
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
```

### 3. Настройка окружения

```bash
cp .env.example .env
```

Отредактируйте `.env`:
```env
DB_PASSWORD=your_secure_database_password
TELEGRAM_BOT_TOKEN=123456:ABC-DEF...
TELEGRAM_BOT_USERNAME=your_bot_username
ENCRYPTION_SECRET_KEY=your_32_byte_base64_key
```

### 4. Запуск

```bash
# Запуск PostgreSQL
docker compose up -d

# Запуск приложения (Windows)
gradlew.bat bootRun

# Запуск приложения (Linux/macOS)
./gradlew bootRun
```

Или откройте проект в IntelliJ IDEA и запустите `TelegramSpyBotApplication`.

## Подключение к Telegram Business

1. Откройте Telegram (нужен Premium)
2. Настройки → Telegram Business → Чатботы
3. Найдите вашего бота и подключите
4. Выберите чаты для мониторинга

## Структура проекта

```
src/main/java/com/spybot/
├── config/           # Конфигурация Spring
├── domain/
│   ├── entity/       # JPA сущности
│   └── enums/        # Перечисления
├── repository/       # Spring Data репозитории
└── service/
    ├── encryption/   # AES-256-GCM шифрование
    ├── handler/      # Обработчики событий Telegram
    └── telegram/     # Telegram Bot сервисы
```

## Конфигурация

| Параметр | Описание | По умолчанию |
|----------|----------|--------------|
| `app.retention.days` | Срок хранения сообщений | 30 |
| `telegram.bot.token` | Токен бота | — |
| `telegram.bot.username` | Username бота | — |
| `encryption.secret-key` | Ключ шифрования (Base64) | — |

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

## Лицензия

MIT
