package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class EquipmentRequest extends VersionedRequest {

    @NotNull(groups = ValidationGroups.Create.class, message = "Укажите версию аудитории")
    @Null(groups = ValidationGroups.Update.class, message = "При редактировании передавайте версию оборудования")
    @PositiveOrZero(message = "Версия аудитории не может быть отрицательной")
    private Long auditoriumVersion;

    public Long getAuditoriumVersion() { return auditoriumVersion; }
    public void setAuditoriumVersion(Long auditoriumVersion) { this.auditoriumVersion = auditoriumVersion; }

}
