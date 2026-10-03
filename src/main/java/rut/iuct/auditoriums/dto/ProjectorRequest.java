package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectorRequest extends EquipmentRequest {

    @Size(max = 255, message = "Допустимо не более 255 символов")
    private String model;

    @Size(max = 255, message = "Допустимо не более 255 символов")
    private String mol;

    @Size(max = 255, message = "Допустимо не более 255 символов")
    private String warehouse;

    public String getModel() { return model; }
    public void setModel(String model) { this.model = TextValues.optional(model); }

    public String getMol() { return mol; }
    public void setMol(String mol) { this.mol = TextValues.optional(mol); }

    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = TextValues.optional(warehouse); }

}
