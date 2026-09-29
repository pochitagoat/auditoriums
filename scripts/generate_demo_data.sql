-- Искусственные данные для всех таблиц аудиторного фонда PostgreSQL.
-- Запустите после миграции V1 через psql или редактор SQL в pgAdmin.
-- Измените row_count ниже для выбора числа новых аудиторий.
-- Скрипт запускается вручную и не является миграцией Flyway.
-- Повторный запуск добавляет новую партию, не меняя существующие строки.

BEGIN;

CREATE TEMP TABLE demo_plan ON COMMIT DROP AS
WITH params AS (
    SELECT
        100::integer AS row_count,
        substring(md5(clock_timestamp()::text || random()::text), 1, 8) AS batch_id
)
SELECT
    g.n AS row_no,
    p.batch_id,
    format('DEMO-%s-%s', p.batch_id, lpad(g.n::text, 4, '0')) AS name,
    (ARRAY[
        'CLASSROOM'::auditorium_type,
        'COMPUTER_CLASSROOM'::auditorium_type,
        'MULTIMEDIA_LECTURE_HALL'::auditorium_type,
        'PRACTICAL_CLASSROOM'::auditorium_type,
        'SERVICE_ROOM'::auditorium_type,
        'SPECIAL_LABORATORY'::auditorium_type,
        'SPECIAL_PURPOSE_ROOM'::auditorium_type
    ])[((g.n - 1) % 7) + 1] AS room_type,
    (ARRAY['Корпус А', 'Корпус Б', 'Корпус В'])[((g.n - 1) % 3) + 1]
        AS room_building
FROM params p
CROSS JOIN LATERAL generate_series(1, p.row_count) AS g(n);

CREATE TEMP TABLE demo_created (
    id BIGINT PRIMARY KEY,
    row_no INTEGER NOT NULL,
    room_type auditorium_type NOT NULL
) ON COMMIT DROP;

WITH inserted AS (
    INSERT INTO auditorium (
        name,
        type,
        description,
        capacity_students,
        capacity_students_extra,
        capacity_staff,
        building,
        floor,
        area,
        has_projector,
        screen_size,
        has_ac,
        pc_count_manual,
        internet,
        subnet,
        in_schedule,
        schedule_link,
        actual_schedule_link,
        last_modified,
        changed_by,
        equipment_issue
    )
    SELECT
        p.name,
        p.room_type,
        format('Искусственно созданная аудитория № %s для проверки приложения.',
            p.row_no),
        CASE WHEN p.room_type = 'SERVICE_ROOM'::auditorium_type
            THEN 0 ELSE 12 + (p.row_no % 9) * 4 END,
        CASE WHEN p.room_type = 'SERVICE_ROOM'::auditorium_type
            THEN 0 ELSE p.row_no % 4 END,
        1 + (p.row_no % 3),
        p.room_building,
        1 + (p.row_no % 5),
        (24 + (p.row_no % 12) * 4.5)::double precision,
        p.row_no % 3 <> 0,
        CASE WHEN p.row_no % 3 <> 0
            THEN format('%s дюймов', 90 + (p.row_no % 4) * 10)
            ELSE NULL END,
        p.row_no % 4 = 0,
        CASE WHEN p.room_type = 'COMPUTER_CLASSROOM'::auditorium_type
            THEN 10 + (p.row_no % 16) ELSE p.row_no % 3 END,
        p.row_no % 5 <> 0,
        CASE WHEN p.row_no % 5 <> 0
            THEN format('10.20.%s.0/24', (p.row_no % 254) + 1)
            ELSE NULL END,
        p.row_no % 6 <> 0,
        CASE WHEN p.row_no % 6 <> 0
            THEN format('https://example.invalid/schedule/%s', p.row_no)
            ELSE NULL END,
        CASE WHEN p.row_no % 6 <> 0
            THEN format('https://example.invalid/schedule/current/%s', p.row_no)
            ELSE NULL END,
        localtimestamp - (p.row_no % 30) * interval '1 day',
        'demo_data_generator',
        CASE WHEN p.row_no % 7 = 0
            THEN 'Тестовая выдача оборудования' ELSE NULL END
    FROM demo_plan p
    RETURNING id, name, type
)
INSERT INTO demo_created (id, row_no, room_type)
SELECT i.id, p.row_no, i.type
FROM inserted i
JOIN demo_plan p ON p.name = i.name;

-- Фото представлены только метаданными. Физические файлы этот SQL не создаёт.
INSERT INTO auditorium_photo (
    auditorium_id, file_path, original_filename, content_type
)
SELECT
    c.id,
    format('auditorium_%s/demo_photo_%s.jpg', c.id, c.row_no),
    format('demo_photo_%s.jpg', c.row_no),
    'image/jpeg'
FROM demo_created c;

INSERT INTO projector (auditorium_id, model, mol, warehouse)
SELECT
    c.id,
    format('Demo Projector %s', 1 + c.row_no % 5),
    format('Тестовый МОЛ %s', 1 + c.row_no % 4),
    'Тестовый склад ОИ'
FROM demo_created c
JOIN auditorium a ON a.id = c.id
WHERE a.has_projector;

-- Количество строк workstation совпадает с pc_count_manual каждой аудитории.
INSERT INTO workstation (
    auditorium_id, pc_name, monitors, keyboards, mice, location_info
)
SELECT
    c.id,
    format('DEMO-PC-%s-%s', c.id, w.number),
    format('Demo Monitor %s', 1 + w.number % 3),
    'Тестовая клавиатура',
    'Тестовая мышь',
    format('Ряд %s, место %s', 1 + (w.number - 1) / 6, w.number)
FROM demo_created c
JOIN auditorium a ON a.id = c.id
CROSS JOIN LATERAL generate_series(1, a.pc_count_manual) AS w(number);

INSERT INTO printer (auditorium_id, model, warehouse, cartridge_link)
SELECT
    c.id,
    format('Demo Printer %s', 1 + c.row_no % 4),
    'Тестовый склад ОИ',
    format('https://example.invalid/cartridges/%s', c.id)
FROM demo_created c
WHERE c.row_no % 4 = 0;

CREATE TEMP TABLE demo_department_ids (
    id BIGINT PRIMARY KEY,
    ordinal INTEGER NOT NULL
) ON COMMIT DROP;

WITH department_source AS (
    SELECT DISTINCT
        p.batch_id,
        d.ordinal,
        d.title
    FROM demo_plan p
    CROSS JOIN (VALUES
        (1, 'Кафедра информатики'),
        (2, 'Учебный отдел'),
        (3, 'Лаборатория связи')
    ) AS d(ordinal, title)
), inserted AS (
    INSERT INTO department (name, mol_link)
    SELECT
        format('DEMO-%s-DEP-%s-%s', batch_id, ordinal, title),
        format('https://example.invalid/mol/%s/%s', batch_id, ordinal)
    FROM department_source
    RETURNING id, name
)
INSERT INTO demo_department_ids (id, ordinal)
SELECT id, substring(name FROM 'DEP-([0-9]+)-')::integer
FROM inserted;

INSERT INTO auditorium_department (auditorium_id, department_id)
SELECT c.id, d.id
FROM demo_created c
JOIN demo_department_ids d ON d.ordinal = 1 + ((c.row_no - 1) % 3);

CREATE TEMP TABLE demo_equipment_ids (
    id BIGINT PRIMARY KEY,
    ordinal INTEGER NOT NULL
) ON COMMIT DROP;

WITH equipment_source AS (
    SELECT DISTINCT
        p.batch_id,
        e.ordinal,
        e.title
    FROM demo_plan p
    CROSS JOIN (VALUES
        (1, 'Телевизор'),
        (2, 'Доска меловая'),
        (3, 'Звуковая система'),
        (4, 'Колонки настенные')
    ) AS e(ordinal, title)
), inserted AS (
    INSERT INTO additional_equipment (name)
    SELECT format('DEMO-%s-EQ-%s-%s', batch_id, ordinal, title)
    FROM equipment_source
    RETURNING id, name
)
INSERT INTO demo_equipment_ids (id, ordinal)
SELECT id, substring(name FROM 'EQ-([0-9]+)-')::integer
FROM inserted;

INSERT INTO auditorium_additional_equipment (
    auditorium_id, additional_equipment_id
)
SELECT c.id, e.id
FROM demo_created c
JOIN demo_equipment_ids e
    ON e.ordinal = 1 + (c.row_no % 4)
    OR (c.row_no % 5 = 0 AND e.ordinal = 4);

-- Итог относится только к строкам, созданным этим запуском.
SELECT
    (SELECT count(*) FROM demo_created) AS auditoriums,
    (SELECT count(*) FROM auditorium_photo
        WHERE auditorium_id IN (SELECT id FROM demo_created)) AS photos,
    (SELECT count(*) FROM projector
        WHERE auditorium_id IN (SELECT id FROM demo_created)) AS projectors,
    (SELECT count(*) FROM workstation
        WHERE auditorium_id IN (SELECT id FROM demo_created)) AS workstations,
    (SELECT count(*) FROM printer
        WHERE auditorium_id IN (SELECT id FROM demo_created)) AS printers,
    (SELECT count(*) FROM demo_department_ids) AS departments,
    (SELECT count(*) FROM auditorium_department
        WHERE auditorium_id IN (SELECT id FROM demo_created)) AS auditorium_departments,
    (SELECT count(*) FROM demo_equipment_ids) AS equipment_types,
    (SELECT count(*) FROM auditorium_additional_equipment
        WHERE auditorium_id IN (SELECT id FROM demo_created)) AS auditorium_equipment;

COMMIT;
