package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class PrinterRequest extends EquipmentRequest {

    @Size(max = 255, message = "Допустимо не более 255 символов")
    private String model;

    @Size(max = 255, message = "Допустимо не более 255 символов")
    private String warehouse;

    @Size(max = 2048, message = "Допустимо не более 2048 символов")
    private String cartridgeLink;

    public String getModel() { return model; }
    public void setModel(String model) { this.model = TextValues.optional(model); }

    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = TextValues.optional(warehouse); }

    public String getCartridgeLink() { return cartridgeLink; }
    public void setCartridgeLink(String cartridgeLink) { this.cartridgeLink = TextValues.optional(cartridgeLink); }

}
