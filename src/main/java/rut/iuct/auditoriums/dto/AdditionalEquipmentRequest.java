package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class AdditionalEquipmentRequest extends VersionedRequest {

    @NotBlank(message = "Укажите название")
    @Size(max = 255, message = "Допустимо не более 255 символов")
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = TextValues.required(name); }

}
