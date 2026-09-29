package rut.iuct.auditoriums.model;

/** Тип аудитории: в БД хранится английское имя константы. */
public enum AuditoriumType {
    CLASSROOM("Кабинет"),
    COMPUTER_CLASSROOM("Компьютерный класс"),
    MULTIMEDIA_LECTURE_HALL("Лекционная мультимедийная"),
    PRACTICAL_CLASSROOM("Аудитория практических занятий"),
    SERVICE_ROOM("Служебная"),
    SPECIAL_LABORATORY("Спецлаборатория"),
    SPECIAL_PURPOSE_ROOM("Специальная");

    private final String displayName;

    AuditoriumType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
