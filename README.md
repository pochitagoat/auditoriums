# Аудитории

Учет аудиторий, оборудования и фотографий университета: поиск, фильтры, редактирование, списание и восстановление техники, справочники кафедр.

**Стек:** Java 21, Spring Boot, PostgreSQL 17, Flyway, Thymeleaf, CSS и JavaScript. «Заявки» и «Статистика» пока содержат заглушки. Авторизации нет; при изменениях вводится ФИО.

Спецификация: [requirements.md](requirements.md).

## Запуск через Docker

Нужны Docker с Linux-контейнерами и Docker Compose. Из корня проекта:

```powershell
Copy-Item .env.example .env
```

Задайте свой `POSTGRES_PASSWORD` в `.env`, затем запустите:

```powershell
docker compose up -d --build --wait
```

Сайт: [localhost:8080](http://localhost:8080). Настройки портов, БД и профиля — в [.env.example](.env.example).

- Логи: `docker compose logs -f app postgres`.
- Остановка: `docker compose stop`; запуск: `docker compose up -d --wait`.
- Обновление кода: `docker compose up -d --build --force-recreate --wait`.

## Запуск через Maven / IDE

Нужны JDK 21 и PostgreSQL 17. Maven Wrapper входит в репозиторий.

```powershell
Copy-Item application-local.properties.example application-local.properties
```

Укажите URL, пользователя и пароль БД в локальном файле. Пример использует порт `5433`; для своей PostgreSQL измените URL. БД из Compose можно запустить командой `docker compose up -d --wait postgres` с настроенным `.env`.

```powershell
.\mvnw.cmd spring-boot:run
```

В IDE запускайте `AuditoriumsApplication`; остановка в терминале — `Ctrl+C`. На Linux/macOS используйте `cp` и `./mvnw`.

Вместо локального файла доступны `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`. Порт приложения задается через `SERVER_PORT`, часовой пояс — `APP_TIME_ZONE` (по умолчанию `Europe/Moscow`). `.env` и `application-local.properties` исключены из Git.

## Данные и фотографии

- Сид: [afterMigrate__development_seed.sql](src/main/resources/db/seed/afterMigrate__development_seed.sql). Профиль `APP_PROFILE=seed` для Compose или `SPRING_PROFILES_ACTIVE=seed` для Maven/IDE заполняет только пустую БД тремя аудиториями и оборудованием; по умолчанию выключен.
- Flyway применяет миграции при запуске. V2 сохраняет исторические данные, переносит связи кафедр и добавляет аудит, версии и статусы; V3 добавляет очередь очистки файлов. Примененные миграции не редактировать. Перед подключением старой БД сделайте копию БД и фотографий; автоматический baseline отключен.
- Фото: JPG/JPEG/PNG до 10 000 000 байт. Локальное хранилище — `PHOTO_STORAGE_ROOT`, по умолчанию `./data/photos`; в Docker — `/app/photos` в volume `photo_data`. Перед сменой каталога остановите приложение и перенесите все файлы с сохранением относительных путей. Для старых абсолютных путей есть [утилита переноса](scripts/migrate_legacy_photos.py).

`stop` и `down` сохраняют volumes БД и фотографий. **Полный сброс тестового окружения:** `docker compose down --volumes` безвозвратно удаляет оба хранилища. Затем включите профиль `seed` и запустите приложение заново. Для отдельного Compose-проекта указывайте одно и то же `--project-name` во всех командах.

## Структура и проверки

- `src/main/java/` — контроллеры, общие сервисы, DTO, сущности, репозитории и хранилище.
- `src/main/resources/` — настройки, миграции, сид, шаблоны, CSS и JavaScript.
- `src/test/java/` — тесты; `scripts/` — копирование, восстановление и дополнительные проверки.

MVC формирует страницы; все изменения интерфейса проходят через `/api/**` и общие сервисы.

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd test
```

JAR создается в `target/`. Тестам нужен Docker: Testcontainers запускает отдельную PostgreSQL. Дополнительные проверки: [интерфейс](scripts/browser_scenarios.cjs), [восстановление](scripts/verify_restore.cjs), [перенос фотографий](scripts/test_legacy_photo_transfer.py). Браузерный сценарий использует Playwright и переменную `AUDITORIUMS_BROWSER_TEST_URL`; запускается на отдельном тестовом окружении и оставляет тестовые записи.

## Резервные копии

Копия включает БД, фотографии и контрольные суммы. Скрипт временно останавливает приложение; другие процессы, меняющие те же данные, тоже должны быть остановлены.

```powershell
.\scripts\backup.ps1 -OutputDirectory backups/manual-copy
.\scripts\restore.ps1 -BackupDirectory backups/manual-copy -ProjectName auditoriums-restore -AppPort 8081 -PostgresPort 5434
```

Скрипты используют `.env` или переменные окружения. Для копирования другого Compose-проекта передайте `-ProjectName` в `backup.ps1`. Восстановление выполняется в новый проект; после него проверьте сайт на [localhost:8081](http://localhost:8081), данные и фотографии.

При Maven/IDE остановите приложение, сделайте `pg_dump -Fc` и скопируйте каталог фотографий. Восстанавливайте dump и файлы вместе в отдельное окружение.

## REST API

Базовый путь — `/api`. Основные ресурсы: `auditoriums`, `departments`, `workstations`, `projectors`, `printers`, `additional-equipment`, `photos`.

Запросы — JSON; загрузка фото — multipart. Для изменений нужен `changedBy`, для обновления/удаления — актуальная `version`, для создания оборудования и фото — `auditoriumVersion`. Создание возвращает `201`, удаление — `204`, конфликт версий — `409`. Поиск: `GET /api/auditoriums?q=301&page=0&size=20`; размеры страницы — 20, 50, 100.

Маршруты и бизнес-правила описаны в [требованиях к API](requirements.md#8-rest-api-и-ошибки), поля запросов — в [DTO](src/main/java/rut/iuct/auditoriums/dto/).
