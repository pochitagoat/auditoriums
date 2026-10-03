package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class StatusChangeRequest extends OperationRequest {

    @PositiveOrZero(message = "Версия не может быть отрицательной")
    private Long version;

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

}
