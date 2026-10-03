package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class OperationRequest {

    @NotBlank(message = "Укажите ФИО")
    @Size(max = 255, message = "ФИО должно содержать не более 255 символов")
    private String changedBy;

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = TextValues.required(changedBy); }

}
