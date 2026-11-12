-- V4__reset_and_reload_development_data.sql
-- Полная очистка демонстрационных данных.
-- Связанные таблицы очищаются через CASCADE.
-- После миграции Flyway выполнит afterMigrate__development_seed.sql.

TRUNCATE TABLE
    auditorium,
    department,
    additional_equipment
RESTART IDENTITY CASCADE;