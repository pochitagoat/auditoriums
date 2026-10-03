package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class WorkstationRequest extends EquipmentRequest {

    @Size(max = 255, message = "Допустимо не более 255 символов")
    private String pcName;

    @Size(max = 255, message = "Допустимо не более 255 символов")
    private String monitors;

    @Size(max = 255, message = "Допустимо не более 255 символов")
    private String keyboards;

    @Size(max = 255, message = "Допустимо не более 255 символов")
    private String mice;

    @Size(max = 255, message = "Допустимо не более 255 символов")
    private String locationInfo;

    public String getPcName() { return pcName; }
    public void setPcName(String pcName) { this.pcName = TextValues.optional(pcName); }

    public String getMonitors() { return monitors; }
    public void setMonitors(String monitors) { this.monitors = TextValues.optional(monitors); }

    public String getKeyboards() { return keyboards; }
    public void setKeyboards(String keyboards) { this.keyboards = TextValues.optional(keyboards); }

    public String getMice() { return mice; }
    public void setMice(String mice) { this.mice = TextValues.optional(mice); }

    public String getLocationInfo() { return locationInfo; }
    public void setLocationInfo(String locationInfo) { this.locationInfo = TextValues.optional(locationInfo); }

}
