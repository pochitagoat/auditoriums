package rut.iuct.auditoriums.controller.mvc;

import org.springframework.stereotype.Component;
import rut.iuct.auditoriums.model.BoardType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component("display")
public class DisplaySupport {
    public String value(Object value) { return value == null || value.toString().isBlank() ? "Не указано" : value.toString(); }
    public String bool(Boolean value) { return value == null ? "Не указано" : value ? "Да" : "Нет"; }
    public String board(BoardType value) { return value == null ? "Не указано" : value == BoardType.CHALK ? "Меловая" : "Маркерная"; }
    public String time(LocalDateTime value) { return value == null ? "Не указано" : value.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")); }
}
