package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectorResponse extends EquipmentResponse {

    private String model;

    private String mol;

    private String warehouse;

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getMol() { return mol; }
    public void setMol(String mol) { this.mol = mol; }

    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }

}
