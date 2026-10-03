package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class WorkstationResponse extends EquipmentResponse {

    private String pcName;

    private String monitors;

    private String keyboards;

    private String mice;

    private String locationInfo;

    public String getPcName() { return pcName; }
    public void setPcName(String pcName) { this.pcName = pcName; }

    public String getMonitors() { return monitors; }
    public void setMonitors(String monitors) { this.monitors = monitors; }

    public String getKeyboards() { return keyboards; }
    public void setKeyboards(String keyboards) { this.keyboards = keyboards; }

    public String getMice() { return mice; }
    public void setMice(String mice) { this.mice = mice; }

    public String getLocationInfo() { return locationInfo; }
    public void setLocationInfo(String locationInfo) { this.locationInfo = locationInfo; }

}
