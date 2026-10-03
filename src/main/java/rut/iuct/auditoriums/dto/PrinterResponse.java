package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class PrinterResponse extends EquipmentResponse {

    private String model;

    private String warehouse;

    private String cartridgeLink;

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }

    public String getCartridgeLink() { return cartridgeLink; }
    public void setCartridgeLink(String cartridgeLink) { this.cartridgeLink = cartridgeLink; }

}
