-- Flyway callback подключается только профилем seed, после V1 и V2.
-- Пустая локальная БД получает небольшой набор данных; существующие данные не меняются.
LOCK TABLE auditorium, auditorium_department, department, additional_equipment,
    workstation, projector, printer, auditorium_photo, auditorium_additional_equipment
    IN ACCESS EXCLUSIVE MODE;

DO $$
DECLARE
    computer_department_id BIGINT;
    mathematics_department_id BIGINT;
    computer_room_id BIGINT;
    lecture_room_id BIGINT;
    service_room_id BIGINT;
    speakers_id BIGINT;
    television_id BIGINT;
    seed_time TIMESTAMP WITHOUT TIME ZONE := CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow';
    seed_author CONSTANT TEXT := 'Демонстрационные данные';
BEGIN
    IF EXISTS (SELECT 1 FROM auditorium)
        OR EXISTS (SELECT 1 FROM department)
        OR EXISTS (SELECT 1 FROM additional_equipment)
        OR EXISTS (SELECT 1 FROM workstation)
        OR EXISTS (SELECT 1 FROM projector)
        OR EXISTS (SELECT 1 FROM printer)
        OR EXISTS (SELECT 1 FROM auditorium_photo)
        OR EXISTS (SELECT 1 FROM auditorium_department)
        OR EXISTS (SELECT 1 FROM auditorium_additional_equipment) THEN
        RAISE NOTICE 'Development seed skipped: the database already contains application data.';
        RETURN;
    END IF;

    INSERT INTO department (name, last_modified, changed_by)
    VALUES ('Кафедра информатики', seed_time, seed_author)
    RETURNING id INTO computer_department_id;

    INSERT INTO department (name, last_modified, changed_by)
    VALUES ('Кафедра математики', seed_time, seed_author)
    RETURNING id INTO mathematics_department_id;

    INSERT INTO additional_equipment (name, last_modified, changed_by)
    VALUES ('Колонки', seed_time, seed_author)
    RETURNING id INTO speakers_id;

    INSERT INTO additional_equipment (name, last_modified, changed_by)
    VALUES ('Телевизор', seed_time, seed_author)
    RETURNING id INTO television_id;

    INSERT INTO auditorium (
        name, type, building, floor, area, capacity_students, capacity_students_extra,
        capacity_staff, department_id, board_type, internet, has_ac, in_schedule,
        has_projector, pc_count_manual, last_modified, changed_by
    ) VALUES (
        '301', 'COMPUTER_CLASSROOM', 'Корпус А', 3, 48, 32, 4,
        1, computer_department_id, 'MARKER', true, false, true,
        true, 2, seed_time, seed_author
    ) RETURNING id INTO computer_room_id;

    INSERT INTO auditorium (
        name, type, building, floor, area, capacity_students, capacity_students_extra,
        capacity_staff, department_id, board_type, internet, has_ac, in_schedule,
        has_projector, pc_count_manual, last_modified, changed_by
    ) VALUES (
        '205', 'MULTIMEDIA_LECTURE_HALL', 'Корпус Б', 2, 90, 60, 0,
        1, mathematics_department_id, 'CHALK', true, true, true,
        false, 1, seed_time, seed_author
    ) RETURNING id INTO lecture_room_id;

    -- Неизвестные значения сохраняются NULL; отрицательный этаж допустим.
    INSERT INTO auditorium (name, type, building, floor, last_modified, changed_by)
    VALUES ('Служебная', 'SERVICE_ROOM', 'Корпус А', -1, seed_time, seed_author)
    RETURNING id INTO service_room_id;

    INSERT INTO workstation (
        auditorium_id, pc_name, monitors, keyboards, mice, location_info,
        status, written_off_at, written_off_by, last_modified, changed_by
    ) VALUES
        (computer_room_id, 'DEMO-INV-001', 'Монитор 24 дюйма', 'USB', 'USB', 'Первый ряд, слева',
            'ACTIVE', NULL, NULL, seed_time, seed_author),
        (computer_room_id, 'DEMO-INV-002', 'Монитор 24 дюйма', 'USB', 'USB', 'Первый ряд, справа',
            'ACTIVE', NULL, NULL, seed_time, seed_author),
        (computer_room_id, 'DEMO-INV-003', 'Монитор 19 дюймов', 'USB', 'USB', 'На хранении',
            'WRITTEN_OFF', seed_time, seed_author, seed_time, seed_author),
        (lecture_room_id, NULL, NULL, NULL, NULL, 'Стол преподавателя',
            'ACTIVE', NULL, NULL, seed_time, seed_author);

    INSERT INTO projector (
        auditorium_id, model, status, written_off_at, written_off_by, last_modified, changed_by
    ) VALUES
        (computer_room_id, 'Демонстрационный проектор', 'ACTIVE', NULL, NULL, seed_time, seed_author),
        (lecture_room_id, 'Старый проектор', 'WRITTEN_OFF', seed_time, seed_author, seed_time, seed_author);

    INSERT INTO printer (
        auditorium_id, model, status, written_off_at, written_off_by, last_modified, changed_by
    ) VALUES
        (computer_room_id, 'Демонстрационный принтер', 'ACTIVE', NULL, NULL, seed_time, seed_author),
        (service_room_id, 'Старый принтер', 'WRITTEN_OFF', seed_time, seed_author, seed_time, seed_author);

    INSERT INTO auditorium_additional_equipment (auditorium_id, additional_equipment_id)
    VALUES (computer_room_id, speakers_id), (lecture_room_id, speakers_id), (lecture_room_id, television_id);

    -- Фотографии добавляются только вместе с настоящими файлами на этапе хранилища.
END;
$$;
