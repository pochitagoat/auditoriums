-- Flyway выполняет эту миграцию в одной транзакции PostgreSQL.
-- Для ручной проверки через psql использовать --single-transaction.
-- Старые значения аудита и пути фотографий сохраняются без подстановки новых данных.
LOCK TABLE auditorium, auditorium_department, department, additional_equipment,
    workstation, projector, printer, auditorium_photo IN ACCESS EXCLUSIVE MODE;

-- Конфликты нужно разрешить явно до переноса, а не выбирать одну из кафедр/записей.
DO $$
DECLARE
    conflicts TEXT;
BEGIN
    SELECT jsonb_agg(to_jsonb(conflict) ORDER BY conflict.auditorium_id)::text
    INTO conflicts
    FROM (
        SELECT auditorium_id, array_agg(department_id ORDER BY department_id) AS department_ids
        FROM auditorium_department
        GROUP BY auditorium_id
        HAVING count(*) > 1
    ) conflict;

    IF conflicts IS NOT NULL THEN
        RAISE EXCEPTION 'Cannot migrate auditoriums linked to multiple departments'
            USING ERRCODE = '23514', DETAIL = conflicts,
                  HINT = 'Choose one department for each listed auditorium before retrying V2. Preserve the original data in a backup.';
    END IF;

    SELECT jsonb_agg(to_jsonb(conflict) ORDER BY conflict.inventory_number)::text
    INTO conflicts
    FROM (
        SELECT btrim(pc_name) AS inventory_number, array_agg(id ORDER BY id) AS workstation_ids
        FROM workstation
        WHERE pc_name IS NOT NULL AND btrim(pc_name) <> ''
        GROUP BY btrim(pc_name)
        HAVING count(*) > 1
    ) conflict;

    IF conflicts IS NOT NULL THEN
        RAISE EXCEPTION 'Cannot enforce unique workstation inventory numbers'
            USING ERRCODE = '23505', DETAIL = conflicts,
                  HINT = 'Resolve the listed duplicate nonblank pc_name values explicitly before retrying V2.';
    END IF;

    SELECT jsonb_agg(to_jsonb(conflict) ORDER BY conflict.table_name, conflict.id)::text
    INTO conflicts
    FROM (
        SELECT 'auditorium' AS table_name, id FROM auditorium WHERE name !~ '[^[:space:]]'
        UNION ALL
        SELECT 'department', id FROM department WHERE name !~ '[^[:space:]]'
        UNION ALL
        SELECT 'additional_equipment', id FROM additional_equipment WHERE name !~ '[^[:space:]]'
    ) conflict;

    IF conflicts IS NOT NULL THEN
        RAISE EXCEPTION 'Cannot enforce nonblank names'
            USING ERRCODE = '23514', DETAIL = conflicts,
                  HINT = 'Fill in the names of the listed records explicitly before retrying V2.';
    END IF;

    SELECT jsonb_agg(id ORDER BY id)::text INTO conflicts
    FROM auditorium
    WHERE area IS NOT NULL AND NOT (area < 'Infinity'::double precision);

    IF conflicts IS NOT NULL THEN
        RAISE EXCEPTION 'Cannot enforce finite auditorium area'
            USING ERRCODE = '23514', DETAIL = conflicts,
                  HINT = 'Correct the listed infinite or NaN area values explicitly before retrying V2.';
    END IF;
END;
$$;

ALTER TABLE auditorium
    ADD COLUMN department_id BIGINT,
    ADD COLUMN board_type VARCHAR(16),
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT fk_auditorium_department
        FOREIGN KEY (department_id) REFERENCES department (id) ON DELETE RESTRICT,
    ADD CONSTRAINT chk_auditorium_board_type
        CHECK (board_type IN ('CHALK', 'MARKER')),
    ADD CONSTRAINT chk_auditorium_version CHECK (version >= 0),
    ADD CONSTRAINT chk_auditorium_name_not_blank CHECK (name ~ '[^[:space:]]'),
    ADD CONSTRAINT chk_auditorium_area_finite CHECK (area < 'Infinity'::double precision);

UPDATE auditorium a
SET department_id = ad.department_id
FROM auditorium_department ad
WHERE ad.auditorium_id = a.id;

-- Сохраняем прежнюю таблицу и все строки как исторический снимок связей.
-- Архивные внешние ключи не должны блокировать удаление разрешенных текущей моделью записей.
ALTER TABLE auditorium_department
    DROP CONSTRAINT fk_auditorium_department_auditorium,
    DROP CONSTRAINT fk_auditorium_department_department;

COMMENT ON TABLE auditorium_department IS
    'Legacy department links retained by V2 for migration verification. The application uses auditorium.department_id; do not write new links here.';

ALTER TABLE department
    ADD COLUMN last_modified TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN changed_by VARCHAR(255),
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT chk_department_version CHECK (version >= 0),
    ADD CONSTRAINT chk_department_name_not_blank CHECK (name ~ '[^[:space:]]');

-- Дополнительное оборудование остается справочником. Полей списания и auditorium_id здесь нет.
ALTER TABLE additional_equipment
    ADD COLUMN last_modified TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN changed_by VARCHAR(255),
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT chk_additional_equipment_version CHECK (version >= 0),
    ADD CONSTRAINT chk_additional_equipment_name_not_blank CHECK (name ~ '[^[:space:]]');

-- Таблица auditorium_additional_equipment, ее ключи и связи остаются без изменений.
ALTER TABLE auditorium_photo
    ADD COLUMN last_modified TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN changed_by VARCHAR(255),
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT chk_auditorium_photo_version CHECK (version >= 0);

ALTER TABLE workstation
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN written_off_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN written_off_by VARCHAR(255),
    ADD COLUMN last_modified TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN changed_by VARCHAR(255),
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT chk_workstation_status CHECK (status IN ('ACTIVE', 'WRITTEN_OFF')),
    ADD CONSTRAINT chk_workstation_write_off CHECK (
        (status = 'ACTIVE' AND written_off_at IS NULL AND written_off_by IS NULL)
        OR (status = 'WRITTEN_OFF' AND written_off_at IS NOT NULL
            AND written_off_by IS NOT NULL AND written_off_by ~ '[^[:space:]]')
    ),
    ADD CONSTRAINT chk_workstation_version CHECK (version >= 0);

ALTER TABLE projector
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN written_off_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN written_off_by VARCHAR(255),
    ADD COLUMN last_modified TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN changed_by VARCHAR(255),
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT chk_projector_status CHECK (status IN ('ACTIVE', 'WRITTEN_OFF')),
    ADD CONSTRAINT chk_projector_write_off CHECK (
        (status = 'ACTIVE' AND written_off_at IS NULL AND written_off_by IS NULL)
        OR (status = 'WRITTEN_OFF' AND written_off_at IS NOT NULL
            AND written_off_by IS NOT NULL AND written_off_by ~ '[^[:space:]]')
    ),
    ADD CONSTRAINT chk_projector_version CHECK (version >= 0);

ALTER TABLE printer
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN written_off_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN written_off_by VARCHAR(255),
    ADD COLUMN last_modified TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN changed_by VARCHAR(255),
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD CONSTRAINT chk_printer_status CHECK (status IN ('ACTIVE', 'WRITTEN_OFF')),
    ADD CONSTRAINT chk_printer_write_off CHECK (
        (status = 'ACTIVE' AND written_off_at IS NULL AND written_off_by IS NULL)
        OR (status = 'WRITTEN_OFF' AND written_off_at IS NOT NULL
            AND written_off_by IS NOT NULL AND written_off_by ~ '[^[:space:]]')
    ),
    ADD CONSTRAINT chk_printer_version CHECK (version >= 0);

CREATE INDEX idx_auditorium_department_id ON auditorium (department_id);
CREATE INDEX idx_auditorium_board_type ON auditorium (board_type);
CREATE INDEX idx_auditorium_list_order ON auditorium (building, name, id);

CREATE INDEX idx_workstation_auditorium_status ON workstation (auditorium_id, status);
CREATE INDEX idx_projector_auditorium_status ON projector (auditorium_id, status);
CREATE INDEX idx_printer_auditorium_status ON printer (auditorium_id, status);

-- Неизвестные и пустые номера допускаются; непустые сравниваются без внешних пробелов.
-- Значения существующих pc_name не изменяются. Уникальность действует и на списанные записи.
CREATE UNIQUE INDEX ux_workstation_pc_name_nonblank ON workstation (btrim(pc_name))
    WHERE pc_name IS NOT NULL AND btrim(pc_name) <> '';
