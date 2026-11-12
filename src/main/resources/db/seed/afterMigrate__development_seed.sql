-- afterMigrate__development_seed.sql
-- Актуальные демонстрационные данные.
-- Существующие данные не меняются; заполняется только пустая БД.

LOCK TABLE auditorium, auditorium_department, department, additional_equipment,
    workstation, projector, printer, auditorium_photo, auditorium_additional_equipment
    IN ACCESS EXCLUSIVE MODE;

DO $$
DECLARE
    seed_time TIMESTAMP WITHOUT TIME ZONE :=
        CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Moscow';
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

    -- Кафедры.
    INSERT INTO department (name, last_modified, changed_by) VALUES
        ('Цифровые технологии управления транспортными процессами', seed_time, seed_author),
        ('Высшая математика', seed_time, seed_author),
        ('Иностранный язык', seed_time, seed_author),
        ('Физика', seed_time, seed_author),
        ('Физическая культура', seed_time, seed_author);

    -- Дополнительное оборудование.
    INSERT INTO additional_equipment (name, last_modified, changed_by) VALUES
        ('Колонки', seed_time, seed_author),
        ('Телевизор', seed_time, seed_author),
        ('Документ-камера', seed_time, seed_author),
        ('Интерактивная доска', seed_time, seed_author),
        ('Система видеосвязи', seed_time, seed_author),
        ('Микрофон', seed_time, seed_author),
        ('Сканер', seed_time, seed_author);

    -- Аудитории.
    INSERT INTO auditorium (
        name, type, description, capacity_students, capacity_students_extra, capacity_staff,
        building, floor, area, has_projector, screen_size, has_ac, pc_count_manual,
        internet, subnet, in_schedule, equipment_issue, department_id, board_type,
        last_modified, changed_by
    ) VALUES
        ('1141', 'CLASSROOM', 'Аудитория для лекций и семинаров', 32, 2, 1,
            'Корпус 1', 1, 52, false, NULL, false, 0, true, '10.10.1.0/24', true, NULL,
            (SELECT id FROM department WHERE name = 'Высшая математика'),
            'CHALK', seed_time, seed_author),
        ('1102', 'PRACTICAL_CLASSROOM', 'Кабинет для практических занятий', 24, 0, 1,
            'Корпус 1', 1, 44, false, NULL, true, 0, true, '10.10.1.0/24', true, NULL,
            (SELECT id FROM department WHERE name = 'Иностранный язык'),
            'MARKER', seed_time, seed_author),
        ('1201', 'MULTIMEDIA_LECTURE_HALL', 'Поточная мультимедийная аудитория', 90, 6, 2,
            'Корпус 1', 2, 128, true, '300 × 170 см', true, 1, true, '10.10.2.0/24', true, NULL,
            (SELECT id FROM department WHERE name = 'Цифровые технологии управления транспортными процессами'),
            'MARKER', seed_time, seed_author),
        ('1202', 'COMPUTER_CLASSROOM', 'Компьютерный класс общего назначения', 24, 2, 1,
            'Корпус 1', 2, 68, true, '240 × 135 см', true, 12, true, '10.10.2.0/24', true, NULL,
            (SELECT id FROM department WHERE name = 'Цифровые технологии управления транспортными процессами'),
            'MARKER', seed_time, seed_author),
        ('1203', 'COMPUTER_CLASSROOM', 'Класс для лабораторных работ по программированию', 18, 0, 1,
            'Корпус 1', 2, 55, false, NULL, false, 8, true, '10.10.2.0/24', true,
            'На одном рабочем месте требуется замена клавиатуры',
            (SELECT id FROM department WHERE name = 'Цифровые технологии управления транспортными процессами'),
            'MARKER', seed_time, seed_author),
        ('1301', 'SPECIAL_LABORATORY', 'Лаборатория цифровой электроники', 16, 0, 2,
            'Корпус 1', 3, 72, true, '200 × 120 см', true, 6, true, '10.10.3.0/24', true, NULL,
            (SELECT id FROM department WHERE name = 'Физика'),
            'MARKER', seed_time, seed_author),
        ('1302', 'CLASSROOM', 'Учебная аудитория кафедры математики', 36, 4, 1,
            'Корпус 1', 3, 61, false, NULL, false, 0, true, '10.10.3.0/24', true, NULL,
            (SELECT id FROM department WHERE name = 'Высшая математика'),
            'CHALK', seed_time, seed_author),
        ('1401', 'MULTIMEDIA_LECTURE_HALL', 'Лекционная аудитория с системой ВКС', 64, 4, 2,
            'Корпус 1', 4, 104, true, '300 × 170 см', true, 0, true, '10.10.4.0/24', true, NULL,
            NULL, 'MARKER', seed_time, seed_author),
        ('1211', 'SPECIAL_PURPOSE_ROOM', 'Зал для конференций и торжественных мероприятий', 180, 20, 5,
            'Корпус 1', 2, 310, true, '500 × 280 см', true, 0, true, '10.10.2.0/24', false,
            'Требуется диагностика правого беспроводного микрофона', NULL,
            'MARKER', seed_time, seed_author),
        ('1108', 'SERVICE_ROOM', 'Служебное помещение центра информационных технологий', NULL, NULL, 3,
            'Корпус 1', 1, 38, NULL, NULL, true, NULL, true, '10.10.0.0/24', false, NULL,
            (SELECT id FROM department WHERE name = 'Цифровые технологии управления транспортными процессами'),
            NULL, seed_time, seed_author),
        ('2105', 'CLASSROOM', 'Аудитория для занятий младших курсов', 28, 2, 1,
            'Корпус 2', 1, 48, false, NULL, false, 0, true, '10.20.1.0/24', true, NULL,
            (SELECT id FROM department WHERE name = 'Высшая математика'),
            'CHALK', seed_time, seed_author),
        ('2106', 'PRACTICAL_CLASSROOM', 'Кабинет разговорной практики', 20, 0, 1,
            'Корпус 2', 1, 42, false, NULL, true, 0, true, '10.20.1.0/24', true, NULL,
            (SELECT id FROM department WHERE name = 'Иностранный язык'),
            'MARKER', seed_time, seed_author),
        ('2204', 'MULTIMEDIA_LECTURE_HALL', 'Лекционная аудитория для потоковых занятий', 72, 8, 2,
            'Корпус 2', 2, 112, true, '300 × 170 см', true, 0, true, '10.20.2.0/24', true, NULL,
            NULL, 'CHALK', seed_time, seed_author),
        ('2205', 'MULTIMEDIA_LECTURE_HALL', 'Аудитория для лекций и видеоконференций', 60, 0, 1,
            'Корпус 2', 2, 90, true, '240 × 135 см', true, 1, true, '10.20.2.0/24', true, NULL,
            (SELECT id FROM department WHERE name = 'Высшая математика'),
            'CHALK', seed_time, seed_author),
        ('2206', 'CLASSROOM', 'Универсальная учебная аудитория', 40, 4, 1,
            'Корпус 2', 2, 66, false, NULL, false, 0, true, '10.20.2.0/24', true,
            'Проектор передан на диагностику', NULL, 'MARKER', seed_time, seed_author),
        ('2303', 'COMPUTER_CLASSROOM', 'Компьютерный класс кафедры радиоэлектроники', 20, 0, 1,
            'Корпус 2', 3, 64, true, '240 × 135 см', true, 10, true, '10.20.3.0/24', true, NULL,
            (SELECT id FROM department WHERE name = 'Физика'),
            'MARKER', seed_time, seed_author),
        ('2304', 'SPECIAL_LABORATORY', 'Лаборатория телекоммуникационных систем', 14, 0, 2,
            'Корпус 2', 3, 70, true, '200 × 120 см', true, 0, true, '10.20.3.0/24', true, NULL,
            (SELECT id FROM department WHERE name = 'Физика'),
            'MARKER', seed_time, seed_author),
        ('2305', 'PRACTICAL_CLASSROOM', 'Кабинет проектной работы', 22, 2, 1,
            'Корпус 2', 3, 50, false, NULL, NULL, 0, true, '10.20.3.0/24', true, NULL,
            NULL, 'MARKER', seed_time, seed_author),
        ('2401', 'CLASSROOM', 'Аудитория для семинаров', 30, 0, 1,
            'Корпус 2', 4, 49, false, NULL, false, 0, true, '10.20.4.0/24', true, NULL,
            (SELECT id FROM department WHERE name = 'Иностранный язык'),
            'CHALK', seed_time, seed_author),
        ('2335', 'SPECIAL_PURPOSE_ROOM', 'Кабинет для занятий с аудиоматериалами', 18, 0, 1,
            'Корпус 2', 3, 46, false, NULL, true, 0, true, '10.20.3.0/24', true, NULL,
            (SELECT id FROM department WHERE name = 'Иностранный язык'),
            'MARKER', seed_time, seed_author),
        ('3101', 'CLASSROOM', 'Аудитория естественно-научного цикла', 34, 2, 1,
            'Корпус 3', 1, 57, false, NULL, false, 0, true, '10.30.1.0/24', true, NULL,
            (SELECT id FROM department WHERE name = 'Высшая математика'),
            'CHALK', seed_time, seed_author),
        ('3102', 'COMPUTER_CLASSROOM', 'Компьютерный класс для самостоятельной работы', 28, 0, 1,
            'Корпус 3', 1, 76, true, '240 × 135 см', true, 14, true, '10.30.1.0/24', true, NULL,
            (SELECT id FROM department WHERE name = 'Цифровые технологии управления транспортными процессами'),
            'MARKER', seed_time, seed_author),
        ('3201', 'MULTIMEDIA_LECTURE_HALL', 'Мультимедийная аудитория', 80, 10, 2,
            'Корпус 3', 2, 126, true, '300 × 170 см', true, 1, true, '10.30.2.0/24', true, NULL,
            NULL, 'MARKER', seed_time, seed_author),
        ('3202', 'SPECIAL_LABORATORY', 'Учебно-исследовательская лаборатория', 12, 0, 3,
            'Корпус 3', 2, 82, true, '200 × 120 см', true, 0, true, '10.30.2.0/24', true,
            'Ожидается замена кабеля документ-камеры',
            (SELECT id FROM department WHERE name = 'Физика'),
            'MARKER', seed_time, seed_author),
        ('3301', 'CLASSROOM', 'Большая аудитория для общих дисциплин', 50, 6, 1,
            'Корпус 3', 3, 84, true, '240 × 135 см', false, 0, true, '10.30.3.0/24', true, NULL,
            NULL, 'CHALK', seed_time, seed_author),
        ('Спортзал', 'SPECIAL_PURPOSE_ROOM', 'Зал для практических занятий физической культурой', 40, 0, 4,
            'Общежитие 5', 1, 420, false, NULL, true, 0, false, NULL, true, NULL,
            (SELECT id FROM department WHERE name = 'Физическая культура'),
            NULL, seed_time, seed_author),
        ('3025', 'SERVICE_ROOM', 'Помещение хранения резервной техники', NULL, NULL, 2,
            'Корпус 3', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, false, NULL,
            NULL, NULL, seed_time, seed_author);

    -- Серийные рабочие места.
    INSERT INTO workstation (
        auditorium_id, pc_name, monitors, keyboards, mice, location_info,
        status, written_off_at, written_off_by, last_modified, changed_by
    )
    SELECT a.id, spec.prefix || lpad(series.number::text, 2, '0'), spec.monitor,
           'Проводная USB', 'Оптическая USB', 'Рабочее место № ' || series.number,
           'ACTIVE', NULL, NULL, seed_time, seed_author
    FROM (VALUES
        ('Корпус 1', '1202', 'A202-PC-', 'Монитор 24 дюйма', 12),
        ('Корпус 1', '1203', 'A203-PC-', 'Монитор 22 дюйма', 8),
        ('Корпус 1', '1301', 'A301-PC-', 'Монитор 24 дюйма', 6),
        ('Корпус 2', '2303', 'B303-PC-', 'Монитор 24 дюйма', 10),
        ('Корпус 3', '3102', 'C102-PC-', 'Монитор 27 дюймов', 14)
    ) AS spec(building, auditorium_name, prefix, monitor, quantity)
    JOIN auditorium a
        ON a.building = spec.building
       AND a.name = spec.auditorium_name
    CROSS JOIN LATERAL generate_series(1, spec.quantity) AS series(number);

    -- Отдельные рабочие места и списанные компьютеры.
    INSERT INTO workstation (
        auditorium_id, pc_name, monitors, keyboards, mice, location_info,
        status, written_off_at, written_off_by, last_modified, changed_by
    )
    SELECT a.id, item.pc_name, item.monitors, item.keyboards, item.mice, item.location_info,
           item.status, item.written_off_at, item.written_off_by, seed_time, seed_author
    FROM (VALUES
        ('Корпус 1', '1201', NULL, 'Монитор 24 дюйма', 'Проводная USB', 'Оптическая USB', 'Стол преподавателя', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 2', '2205', NULL, 'Монитор 24 дюйма', 'Проводная USB', 'Оптическая USB', 'Стол преподавателя', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 3', '3201', NULL, 'Монитор 27 дюймов', 'Проводная USB', 'Оптическая USB', 'Стол преподавателя', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 1', '1202', 'A202-PC-OLD', 'Монитор 19 дюймов', 'PS/2', 'PS/2', 'Резерв', 'WRITTEN_OFF', seed_time, seed_author),
        ('Корпус 2', '2303', 'B303-PC-OLD', 'Монитор 19 дюймов', 'PS/2', 'PS/2', 'Склад', 'WRITTEN_OFF', seed_time, seed_author),
        ('Корпус 3', '3102', 'C102-PC-OLD', 'Монитор 20 дюймов', 'Проводная USB', 'Оптическая USB', 'Резерв', 'WRITTEN_OFF', seed_time, seed_author)
    ) AS item(building, auditorium_name, pc_name, monitors, keyboards, mice, location_info,
              status, written_off_at, written_off_by)
    JOIN auditorium a
        ON a.building = item.building
       AND a.name = item.auditorium_name;

    -- Проекторы.
    INSERT INTO projector (
        auditorium_id, model, mol, warehouse, status,
        written_off_at, written_off_by, last_modified, changed_by
    )
    SELECT a.id, item.model, item.mol, item.warehouse, item.status,
           item.written_off_at, item.written_off_by, seed_time, seed_author
    FROM (VALUES
        ('Корпус 1', '1201', 'Epson EB-L200F', 'Иванов И.И.', 'А-201', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 1', '1202', 'BenQ MH560', 'Петрова А.С.', 'А-202', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 1', '1301', 'Epson EB-982W', 'Сидоров П.Н.', 'А-301', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 1', '1401', 'ViewSonic LS740W', 'Иванов И.И.', 'А-401', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 1', '1211', 'Epson EB-L630U', 'Иванов И.И.', 'Актовый зал', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 2', '2204', 'BenQ LU930', 'Смирнова Е.В.', 'Б-204', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 2', '2205', 'Epson EB-W06', 'Смирнова Е.В.', 'Б-205', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 2', '2303', 'Acer X1527i', 'Кузнецов Д.А.', 'Б-303', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 2', '2304', 'Epson EB-2250U', 'Кузнецов Д.А.', 'Б-304', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 3', '3102', 'BenQ TH685i', 'Орлова М.В.', 'В-102', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 3', '3201', 'ViewSonic PA503W', 'Орлова М.В.', 'В-201', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 3', '3202', 'Epson EB-992F', 'Кузнецов Д.А.', 'В-202', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 1', '1203', 'Acer P1265', 'Петрова А.С.', 'Склад А', 'WRITTEN_OFF', seed_time, seed_author),
        ('Корпус 2', '2206', 'Epson EB-X02', 'Смирнова Е.В.', 'Склад Б', 'WRITTEN_OFF', seed_time, seed_author)
    ) AS item(building, auditorium_name, model, mol, warehouse, status, written_off_at, written_off_by)
    JOIN auditorium a
        ON a.building = item.building
       AND a.name = item.auditorium_name;

    -- Принтеры.
    INSERT INTO printer (
        auditorium_id, model, warehouse, cartridge_link, status,
        written_off_at, written_off_by, last_modified, changed_by
    )
    SELECT a.id, item.model, item.warehouse, item.cartridge_link, item.status,
           item.written_off_at, item.written_off_by, seed_time, seed_author
    FROM (VALUES
        ('Корпус 1', '1202', 'HP LaserJet Pro M404dn', 'А-202', 'https://example.test/cartridges/hp-59a', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 1', '1203', 'Brother HL-L5100DN', 'А-203', 'https://example.test/cartridges/tn-3480', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 1', '1301', 'Canon i-SENSYS LBP223dw', 'А-301', 'https://example.test/cartridges/canon-057', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 1', '1108', 'Kyocera ECOSYS M2040dn', 'ИТ-служба', 'https://example.test/cartridges/tk-1170', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 2', '2303', 'HP LaserJet Pro M404dw', 'Б-303', 'https://example.test/cartridges/hp-59a', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 2', '2401', 'Xerox B310', 'Б-401', 'https://example.test/cartridges/xerox-006r04380', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 3', '3102', 'Brother DCP-L2540DNR', 'В-102', 'https://example.test/cartridges/tn-2375', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 3', '3025', 'HP LaserJet Enterprise M507dn', 'Склад В', 'https://example.test/cartridges/hp-89a', 'ACTIVE', NULL::timestamp, NULL),
        ('Корпус 1', '1141', 'HP LaserJet 1020', 'Склад А', NULL, 'WRITTEN_OFF', seed_time, seed_author),
        ('Корпус 2', '2105', 'Samsung ML-2160', 'Склад Б', NULL, 'WRITTEN_OFF', seed_time, seed_author)
    ) AS item(building, auditorium_name, model, warehouse, cartridge_link, status, written_off_at, written_off_by)
    JOIN auditorium a
        ON a.building = item.building
       AND a.name = item.auditorium_name;

    -- Связи аудиторий с дополнительным оборудованием.
    INSERT INTO auditorium_additional_equipment (auditorium_id, additional_equipment_id)
    SELECT a.id, equipment.id
    FROM (VALUES
        ('Корпус 1', '1141', 'Телевизор'),
        ('Корпус 1', '1102', 'Интерактивная доска'),
        ('Корпус 1', '1201', 'Колонки'),
        ('Корпус 1', '1201', 'Документ-камера'),
        ('Корпус 1', '1202', 'Колонки'),
        ('Корпус 1', '1301', 'Документ-камера'),
        ('Корпус 1', '1401', 'Колонки'),
        ('Корпус 1', '1401', 'Система видеосвязи'),
        ('Корпус 1', '1211', 'Колонки'),
        ('Корпус 1', '1211', 'Микрофон'),
        ('Корпус 1', '1211', 'Система видеосвязи'),
        ('Корпус 1', '1108', 'Сканер'),
        ('Корпус 2', '2106', 'Телевизор'),
        ('Корпус 2', '2204', 'Колонки'),
        ('Корпус 2', '2204', 'Документ-камера'),
        ('Корпус 2', '2205', 'Колонки'),
        ('Корпус 2', '2205', 'Система видеосвязи'),
        ('Корпус 2', '2303', 'Интерактивная доска'),
        ('Корпус 2', '2304', 'Документ-камера'),
        ('Корпус 2', '2305', 'Интерактивная доска'),
        ('Корпус 2', '2106', 'Колонки'),
        ('Корпус 3', '3101', 'Телевизор'),
        ('Корпус 3', '3102', 'Колонки'),
        ('Корпус 3', '3201', 'Колонки'),
        ('Корпус 3', '3201', 'Документ-камера'),
        ('Корпус 3', '3202', 'Документ-камера'),
        ('Корпус 3', '3202', 'Интерактивная доска'),
        ('Корпус 3', '3301', 'Телевизор'),
        ('Общежитие 5', 'Спортзал', 'Колонки'),
        ('Корпус 3', '3025', 'Сканер')
    ) AS link(building, auditorium_name, equipment_name)
    JOIN auditorium a
        ON a.building = link.building
       AND a.name = link.auditorium_name
    JOIN additional_equipment equipment
        ON equipment.name = link.equipment_name;

    -- Фотографии не добавляются: они загружаются через приложение
    -- вместе с соответствующими файлами.
END;
$$;
