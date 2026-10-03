package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class EquipmentLinkRequest extends OperationRequest {

    @NotNull(message = "Выберите тип оборудования")
    @Positive(message = "Идентификатор должен быть положительным")
    private Long additionalEquipmentId;

    @NotNull(message = "Укажите версию аудитории")
    @PositiveOrZero(message = "Версия не может быть отрицательной")
    private Long auditoriumVersion;

    public Long getAdditionalEquipmentId() { return additionalEquipmentId; }
    public void setAdditionalEquipmentId(Long additionalEquipmentId) { this.additionalEquipmentId = additionalEquipmentId; }

    public Long getAuditoriumVersion() { return auditoriumVersion; }
    public void setAuditoriumVersion(Long auditoriumVersion) { this.auditoriumVersion = auditoriumVersion; }

}
