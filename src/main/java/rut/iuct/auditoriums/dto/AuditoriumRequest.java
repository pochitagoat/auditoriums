package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class AuditoriumRequest extends VersionedRequest {

    @NotBlank(message = "Укажите название")
    @Size(max = 255, message = "Допустимо не более 255 символов")
    private String name;

    @NotNull(message = "Выберите тип аудитории")
    private AuditoriumType type;

    private String description;

    @PositiveOrZero(message = "Значение не может быть отрицательным")
    private Integer capacityStudents;

    @PositiveOrZero(message = "Значение не может быть отрицательным")
    private Integer capacityStudentsExtra;

    @PositiveOrZero(message = "Значение не может быть отрицательным")
    private Integer capacityStaff;

    @Size(max = 255, message = "Допустимо не более 255 символов")
    private String building;

    private Integer floor;

    @PositiveOrZero(message = "Площадь не может быть отрицательной")
    private Double area;

    @Size(max = 255, message = "Допустимо не более 255 символов")
    private String screenSize;

    private Boolean hasAc;

    private Boolean internet;

    @Size(max = 255, message = "Допустимо не более 255 символов")
    private String subnet;

    private Boolean inSchedule;

    @Size(max = 2048, message = "Допустимо не более 2048 символов")
    private String equipmentIssue;

    @Positive(message = "Идентификатор должен быть положительным")
    private Long departmentId;

    private BoardType boardType;

    public String getName() { return name; }
    public void setName(String name) { this.name = TextValues.required(name); }

    public AuditoriumType getType() { return type; }
    public void setType(AuditoriumType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = TextValues.optional(description); }

    public Integer getCapacityStudents() { return capacityStudents; }
    public void setCapacityStudents(Integer capacityStudents) { this.capacityStudents = capacityStudents; }

    public Integer getCapacityStudentsExtra() { return capacityStudentsExtra; }
    public void setCapacityStudentsExtra(Integer capacityStudentsExtra) { this.capacityStudentsExtra = capacityStudentsExtra; }

    public Integer getCapacityStaff() { return capacityStaff; }
    public void setCapacityStaff(Integer capacityStaff) { this.capacityStaff = capacityStaff; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = TextValues.optional(building); }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public Double getArea() { return area; }
    public void setArea(Double area) { this.area = area; }

    public String getScreenSize() { return screenSize; }
    public void setScreenSize(String screenSize) { this.screenSize = TextValues.optional(screenSize); }

    public Boolean getHasAc() { return hasAc; }
    public void setHasAc(Boolean hasAc) { this.hasAc = hasAc; }

    public Boolean getInternet() { return internet; }
    public void setInternet(Boolean internet) { this.internet = internet; }

    public String getSubnet() { return subnet; }
    public void setSubnet(String subnet) { this.subnet = TextValues.optional(subnet); }

    public Boolean getInSchedule() { return inSchedule; }
    public void setInSchedule(Boolean inSchedule) { this.inSchedule = inSchedule; }

    public String getEquipmentIssue() { return equipmentIssue; }
    public void setEquipmentIssue(String equipmentIssue) { this.equipmentIssue = TextValues.optional(equipmentIssue); }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public BoardType getBoardType() { return boardType; }
    public void setBoardType(BoardType boardType) { this.boardType = boardType; }

    @AssertTrue(message = "Площадь должна быть конечным числом")
    public boolean isAreaFinite() { return area == null || Double.isFinite(area); }
}
