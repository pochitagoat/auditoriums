package rut.iuct.auditoriums.service;

import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import rut.iuct.auditoriums.dto.OperationRequest;
import rut.iuct.auditoriums.dto.StatusChangeRequest;
import rut.iuct.auditoriums.dto.TextValues;
import rut.iuct.auditoriums.exception.ApiException;
import rut.iuct.auditoriums.model.*;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class AuditSupport {
    private final Validator validator;
    private final Clock clock;

    public AuditSupport(Validator validator, Clock clock) {
        this.validator = validator;
        this.clock = clock;
    }

    public void validate(Object request, Class<?>... groups) {
        Map<String, String> errors = new LinkedHashMap<>();
        validator.validate(request, groups).forEach(v -> errors.putIfAbsent(fieldName(v.getPropertyPath().toString()), v.getMessage()));
        if (!errors.isEmpty()) throw new ApiException(HttpStatus.BAD_REQUEST, "Проверьте заполнение формы", errors);
    }

    public static String fieldName(String path) {
        String field = path.substring(path.lastIndexOf('.') + 1);
        return switch (field) {
            case "areaFinite" -> "area";
            case "pageSizeAllowed" -> "size";
            case "offsetAllowed" -> "page";
            default -> field;
        };
    }

    public void validateDeletion(String changedBy, Long version) {
        OperationRequest operation = new OperationRequest();
        operation.setChangedBy(changedBy);
        validate(operation);
        requireVersion(version);
        if (version < 0) throw new ApiException(HttpStatus.BAD_REQUEST, "Версия не может быть отрицательной", Map.of("version", "Версия не может быть отрицательной"));
    }

    public void requireVersion(Long version) {
        if (version == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Укажите версию записи", Map.of("version", "Укажите версию записи"));
    }

    public void checkVersion(AuditedEntity entity, Long expectedVersion) {
        requireVersion(expectedVersion);
        if (!Objects.equals(entity.getVersion(), expectedVersion)) {
            throw new ApiException(HttpStatus.CONFLICT, "Запись уже изменена. Обновите данные и повторите операцию",
                    Map.of("version", "Использована устаревшая версия записи"));
        }
    }

    public void touch(AuditedEntity entity, String changedBy) {
        LocalDateTime now = LocalDateTime.now(clock).truncatedTo(ChronoUnit.MICROS);
        // Даже две операции одного автора в одну микросекунду должны изменить аудит и @Version.
        if (now.equals(entity.getLastModified()) && Objects.equals(entity.getChangedBy(), TextValues.required(changedBy))) {
            now = now.plusNanos(1000);
        }
        entity.setLastModified(now);
        entity.setChangedBy(TextValues.required(changedBy));
    }

    public boolean changeStatus(WriteOffEquipment equipment, EquipmentStatus desired, StatusChangeRequest request) {
        validate(request);
        if (equipment.getStatus() == desired) return false;
        checkVersion(equipment, request.getVersion());
        touch(equipment, request.getChangedBy());
        equipment.setStatus(desired);
        equipment.setWrittenOffAt(desired == EquipmentStatus.WRITTEN_OFF ? equipment.getLastModified() : null);
        equipment.setWrittenOffBy(desired == EquipmentStatus.WRITTEN_OFF ? request.getChangedBy() : null);
        return true;
    }
}
