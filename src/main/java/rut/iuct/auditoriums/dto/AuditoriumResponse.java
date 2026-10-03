package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class AuditoriumResponse extends AuditoriumListItem {

    private String description;

    private Integer capacityStudentsExtra;

    private Integer capacityStaff;

    private Long totalCapacityStudents;

    private String screenSize;

    private String subnet;

    private String equipmentIssue;

    private Boolean legacyHasProjector;

    private Integer legacyPcCountManual;

    private String legacyEquipmentWarning;

    private List<AdditionalEquipmentResponse> additionalEquipment;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCapacityStudentsExtra() { return capacityStudentsExtra; }
    public void setCapacityStudentsExtra(Integer capacityStudentsExtra) { this.capacityStudentsExtra = capacityStudentsExtra; }

    public Integer getCapacityStaff() { return capacityStaff; }
    public void setCapacityStaff(Integer capacityStaff) { this.capacityStaff = capacityStaff; }

    public Long getTotalCapacityStudents() { return totalCapacityStudents; }
    public void setTotalCapacityStudents(Long totalCapacityStudents) { this.totalCapacityStudents = totalCapacityStudents; }

    public String getScreenSize() { return screenSize; }
    public void setScreenSize(String screenSize) { this.screenSize = screenSize; }

    public String getSubnet() { return subnet; }
    public void setSubnet(String subnet) { this.subnet = subnet; }

    public String getEquipmentIssue() { return equipmentIssue; }
    public void setEquipmentIssue(String equipmentIssue) { this.equipmentIssue = equipmentIssue; }

    public Boolean getLegacyHasProjector() { return legacyHasProjector; }
    public void setLegacyHasProjector(Boolean legacyHasProjector) { this.legacyHasProjector = legacyHasProjector; }

    public Integer getLegacyPcCountManual() { return legacyPcCountManual; }
    public void setLegacyPcCountManual(Integer legacyPcCountManual) { this.legacyPcCountManual = legacyPcCountManual; }

    public String getLegacyEquipmentWarning() { return legacyEquipmentWarning; }
    public void setLegacyEquipmentWarning(String legacyEquipmentWarning) { this.legacyEquipmentWarning = legacyEquipmentWarning; }

    public List<AdditionalEquipmentResponse> getAdditionalEquipment() { return additionalEquipment; }
    public void setAdditionalEquipment(List<AdditionalEquipmentResponse> additionalEquipment) { this.additionalEquipment = additionalEquipment; }

}
