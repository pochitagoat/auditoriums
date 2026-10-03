package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class AuditoriumListItem extends AuditedResponse {

    private String name;

    private String building;

    private AuditoriumType type;

    private Integer floor;

    private Double area;

    private Integer capacityStudents;

    private BoardType boardType;

    private DepartmentResponse department;

    private Boolean internet;

    private Boolean hasAc;

    private Boolean inSchedule;

    private Long activeWorkstationCount;

    private boolean hasActiveProjector;

    private boolean hasActivePrinter;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public AuditoriumType getType() { return type; }
    public void setType(AuditoriumType type) { this.type = type; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public Double getArea() { return area; }
    public void setArea(Double area) { this.area = area; }

    public Integer getCapacityStudents() { return capacityStudents; }
    public void setCapacityStudents(Integer capacityStudents) { this.capacityStudents = capacityStudents; }

    public BoardType getBoardType() { return boardType; }
    public void setBoardType(BoardType boardType) { this.boardType = boardType; }

    public DepartmentResponse getDepartment() { return department; }
    public void setDepartment(DepartmentResponse department) { this.department = department; }

    public Boolean getInternet() { return internet; }
    public void setInternet(Boolean internet) { this.internet = internet; }

    public Boolean getHasAc() { return hasAc; }
    public void setHasAc(Boolean hasAc) { this.hasAc = hasAc; }

    public Boolean getInSchedule() { return inSchedule; }
    public void setInSchedule(Boolean inSchedule) { this.inSchedule = inSchedule; }

    public Long getActiveWorkstationCount() { return activeWorkstationCount; }
    public void setActiveWorkstationCount(Long activeWorkstationCount) { this.activeWorkstationCount = activeWorkstationCount; }

    public boolean getHasActiveProjector() { return hasActiveProjector; }
    public void setHasActiveProjector(boolean hasActiveProjector) { this.hasActiveProjector = hasActiveProjector; }

    public boolean getHasActivePrinter() { return hasActivePrinter; }
    public void setHasActivePrinter(boolean hasActivePrinter) { this.hasActivePrinter = hasActivePrinter; }

}
