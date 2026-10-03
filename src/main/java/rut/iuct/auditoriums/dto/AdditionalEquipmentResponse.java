package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class AdditionalEquipmentResponse extends AuditedResponse {

    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}
