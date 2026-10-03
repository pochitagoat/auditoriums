package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class AuditoriumFilter {

    private String q;

    private AuditoriumType type;

    private String building;

    private Integer floor;

    private Boolean hasProjector;

    private Boolean hasPrinter;

    private Boolean hasWorkstations;

    private Boolean internet;

    private Boolean hasAc;

    private BoardType boardType;

    @Positive(message = "Идентификатор должен быть положительным")
    private Long departmentId;

    @NotNull(message = "Укажите номер страницы")
    @PositiveOrZero(message = "Номер страницы не может быть отрицательным")
    private Integer page = 0;

    @NotNull(message = "Укажите размер страницы")
    private Integer size = 20;

    public String getQ() { return q; }
    public void setQ(String q) { this.q = TextValues.optional(q); }

    public AuditoriumType getType() { return type; }
    public void setType(AuditoriumType type) { this.type = type; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = TextValues.optional(building); }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public Boolean getHasProjector() { return hasProjector; }
    public void setHasProjector(Boolean hasProjector) { this.hasProjector = hasProjector; }

    public Boolean getHasPrinter() { return hasPrinter; }
    public void setHasPrinter(Boolean hasPrinter) { this.hasPrinter = hasPrinter; }

    public Boolean getHasWorkstations() { return hasWorkstations; }
    public void setHasWorkstations(Boolean hasWorkstations) { this.hasWorkstations = hasWorkstations; }

    public Boolean getInternet() { return internet; }
    public void setInternet(Boolean internet) { this.internet = internet; }

    public Boolean getHasAc() { return hasAc; }
    public void setHasAc(Boolean hasAc) { this.hasAc = hasAc; }

    public BoardType getBoardType() { return boardType; }
    public void setBoardType(BoardType boardType) { this.boardType = boardType; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }

    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }

    @AssertTrue(message = "Размер страницы должен быть 20, 50 или 100")
    public boolean isPageSizeAllowed() { return size != null && List.of(20, 50, 100).contains(size); }

    @AssertTrue(message = "Номер страницы слишком велик")
    public boolean isOffsetAllowed() {
        return page == null || size == null || (long) page * size <= Integer.MAX_VALUE;
    }
}
