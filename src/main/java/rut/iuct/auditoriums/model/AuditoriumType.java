package rut.iuct.auditoriums.model;

/** Тип аудитории: в БД хранится английское имя константы. */
public enum AuditoriumType {
    CLASSROOM("Обычная аудитория"),
    COMPUTER_CLASSROOM("Компьютерная аудитория"),
    MULTIMEDIA_LECTURE_HALL("Мультимедийная лекционная аудитория"),
    PRACTICAL_CLASSROOM("Аудитория для практических занятий"),
    SERVICE_ROOM("Служебное помещение"),
    SPECIAL_LABORATORY("Специализированная лаборатория"),
    SPECIAL_PURPOSE_ROOM("Помещение специального назначения");

    private final String displayName;

    AuditoriumType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
