# Telegram Spy Bot

Бот для отслеживания удалённых и изменённых сообщений в Telegram через Telegram Business API.

## Как это работает

1. Пользователь создаёт бота через @BotFather
2. Пользователь с Telegram Premium подключает бота к своему "Telegram для бизнеса"
3. Бот получает копии всех сообщений в выбранных чатах
4. При удалении или изменении сообщения пользователь получает уведомление в бота

## Безопасность

### Шифрование данных
- Все тексты сообщений шифруются **AES-256-GCM** перед сохранением в БД
- Ключ шифрования хранится только в переменных окружения
- Даже при утечке БД — данные нечитаемы

### Логирование
- В логах **НЕ сохраняется** содержимое сообщений
- Логируются только технические события (ID, timestamps, типы событий)
- Вы (как администратор) **НЕ увидите** тексты сообщений в логах

### Доступ к данным
- Расшифрованные сообщения видит только владелец бизнес-подключения через Telegram
- Нет административного интерфейса для просмотра сообщений
- Данные автоматически удаляются через 30 дней (настраивается)

## Требования

- Java 21+
- PostgreSQL 14+
- Docker (опционально)
- Telegram Bot Token (от @BotFather)

## Быстрый старт

### 1. Создание бота

1. Напишите @BotFather в Telegram
2. Создайте нового бота: `/newbot`
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
# Скопируйте пример конфигурации
cp .env.example .env

# Отредактируйте .env файл
```

Содержимое `.env`:
```env
DB_PASSWORD=your_secure_database_password
TELEGRAM_BOT_TOKEN=123456:ABC-DEF...
TELEGRAM_BOT_USERNAME=your_bot_username
ENCRYPTION_SECRET_KEY=your_32_byte_base64_key
```

### 4. Запуск с Docker Compose

```bash
# Запуск БД и приложения
docker-compose up -d

# Просмотр логов
docker-compose logs -f app
```

### 5. Запуск для разработки (Windows)

```bash
# Запустите только PostgreSQL
docker-compose up -d postgres

# Соберите и запустите приложение
gradlew.bat bootRun
```

### 5. Запуск для разработки (Linux/macOS)

```bash
# Запустите только PostgreSQL
docker-compose up -d postgres

# Соберите и запустите приложение
./gradlew bootRun
```

## Подключение бота к Telegram Business

1. Откройте Telegram с Premium-аккаунтом
2. Настройки → Telegram Business → Чатботы
3. Найдите вашего бота и подключите
4. Выберите чаты для мониторинга
5. Готово! Бот пришлёт подтверждение

## Структура проекта

```
src/main/java/com/spybot/
├── TelegramSpyBotApplication.java    # Точка входа
├── config/                           # Конфигурация Spring
├── domain/
│   ├── entity/                       # JPA сущности
│   └── enums/                        # Перечисления
├── repository/                       # Spring Data репозитории
└── service/
    ├── encryption/                   # Сервис шифрования
    ├── handler/                      # Обработчики событий Telegram
    └── telegram/                     # Telegram Bot сервисы
```

## API Events (Telegram Business)

| Событие | Описание |
|---------|----------|
| `business_connection` | Пользователь подключил/отключил бота |
| `business_message` | Новое сообщение в отслеживаемом чате |
| `edited_business_message` | Сообщение было изменено |
| `deleted_business_messages` | Сообщения были удалены |

## Конфигурация

| Параметр | Описание | По умолчанию |
|----------|----------|--------------|
| `app.retention.days` | Срок хранения сообщений (дней) | 30 |
| `telegram.bot.token` | Токен бота | — |
| `telegram.bot.username` | Username бота | — |
| `encryption.secret-key` | Ключ шифрования (Base64) | — |

## Деплой на Ubuntu

```bash
# Установка Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Установка Docker Compose
sudo apt install docker-compose-plugin

# Клонирование и запуск
git clone <your-repo>
cd telegram-spy-bot
cp .env.example .env
# Отредактируйте .env
docker compose up -d
```

## Мониторинг

```bash
# Проверка здоровья
curl http://localhost:8080/actuator/health

# Просмотр логов
docker-compose logs -f app

# Метрики
curl http://localhost:8080/actuator/metrics
```

## Troubleshooting

### Бот не получает сообщения
1. Убедитесь, что Business Mode включен в @BotFather
2. Проверьте, что у вас Telegram Premium
3. Убедитесь, что бот подключен в настройках Telegram Business

### Ошибка шифрования
- Проверьте, что `ENCRYPTION_SECRET_KEY` — это валидная Base64 строка длиной 32 байта

### Ошибка подключения к БД
- Проверьте, что PostgreSQL запущен: `docker-compose ps`
- Проверьте переменные окружения в `.env`

## Лицензия

MIT
