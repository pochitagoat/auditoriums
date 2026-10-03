package rut.iuct.auditoriums.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Аудитория и связанное с ней имущество. */
@Entity
@Table(name = "auditorium")
public class Auditorium extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false, columnDefinition = "auditorium_type")
    private AuditoriumType type;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @PositiveOrZero
    @Column(name = "capacity_students")
    private Integer capacityStudents;

    @PositiveOrZero
    @Column(name = "capacity_students_extra")
    private Integer capacityStudentsExtra;

    @PositiveOrZero
    @Column(name = "capacity_staff")
    private Integer capacityStaff;

    @Size(max = 255)
    @Column(name = "building")
    private String building;

    @Column(name = "floor")
    private Integer floor;

    @PositiveOrZero
    @Column(name = "area")
    private Double area;

    /** Исторический признак; действующее наличие вычисляется по projector. */
    @Column(name = "has_projector")
    private Boolean hasProjector;

    @Size(max = 255)
    @Column(name = "screen_size")
    private String screenSize;

    @Column(name = "has_ac")
    private Boolean hasAc;

    /** Историческое число; действующее количество вычисляется по workstation. */
    @PositiveOrZero
    @Column(name = "pc_count_manual")
    private Integer pcCountManual;

    @Column(name = "internet")
    private Boolean internet;

    @Size(max = 255)
    @Column(name = "subnet")
    private String subnet;

    @Column(name = "in_schedule")
    private Boolean inSchedule;

    @Size(max = 2048)
    @Column(name = "schedule_link", length = 2048)
    private String scheduleLink;

    @Size(max = 2048)
    @Column(name = "actual_schedule_link", length = 2048)
    private String actualScheduleLink;

    @Size(max = 2048)
    @Column(name = "equipment_issue", length = 2048)
    private String equipmentIssue;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_type", length = 16)
    private BoardType boardType;

    /** Вычисления отражают сохраненные активные записи при загрузке Entity. */
    @Formula("(select count(*) from workstation w where w.auditorium_id = id and w.status = 'ACTIVE')")
    private Long pcCountAuto;

    @Formula("exists (select 1 from projector p where p.auditorium_id = id and p.status = 'ACTIVE')")
    private boolean hasActiveProjector;

    @Formula("exists (select 1 from printer p where p.auditorium_id = id and p.status = 'ACTIVE')")
    private boolean hasActivePrinter;

    @OneToMany(mappedBy = "auditorium")
    private List<AuditoriumPhoto> photos = new ArrayList<>();

    @OneToMany(mappedBy = "auditorium")
    private List<Projector> projectors = new ArrayList<>();

    @OneToMany(mappedBy = "auditorium")
    private List<Workstation> workstations = new ArrayList<>();

    @OneToMany(mappedBy = "auditorium")
    private List<Printer> printers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToMany(mappedBy = "auditorium", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AuditoriumAdditionalEquipment> additionalEquipmentLinks = new HashSet<>();

    public Auditorium() {
    }

    public Auditorium(
            Long id,
            String name,
            AuditoriumType type,
            String description,
            Integer capacityStudents,
            Integer capacityStudentsExtra,
            Integer capacityStaff,
            String building,
            Integer floor,
            Double area,
            Boolean hasProjector,
            String screenSize,
            Boolean hasAc,
            Integer pcCountManual,
            Boolean internet,
            String subnet,
            Boolean inSchedule,
            String scheduleLink,
            String actualScheduleLink,
            LocalDateTime lastModified,
            String changedBy,
            String equipmentIssue,
            List<AuditoriumPhoto> photos,
            List<Projector> projectors,
            List<Workstation> workstations,
            List<Printer> printers,
            Department department,
            Set<AdditionalEquipment> additionalEquipment
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.capacityStudents = capacityStudents;
        this.capacityStudentsExtra = capacityStudentsExtra;
        this.capacityStaff = capacityStaff;
        this.building = building;
        this.floor = floor;
        this.area = area;
        this.hasProjector = hasProjector;
        this.screenSize = screenSize;
        this.hasAc = hasAc;
        this.pcCountManual = pcCountManual;
        this.internet = internet;
        this.subnet = subnet;
        this.inSchedule = inSchedule;
        this.scheduleLink = scheduleLink;
        this.actualScheduleLink = actualScheduleLink;
        setLastModified(lastModified);
        setChangedBy(changedBy);
        this.equipmentIssue = equipmentIssue;
        setPhotos(photos);
        setProjectors(projectors);
        setWorkstations(workstations);
        setPrinters(printers);
        setDepartment(department);
        setAdditionalEquipment(additionalEquipment);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public AuditoriumType getType() { return type; }
    public void setType(AuditoriumType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCapacityStudents() { return capacityStudents; }
    public void setCapacityStudents(Integer capacityStudents) { this.capacityStudents = capacityStudents; }

    public Integer getCapacityStudentsExtra() { return capacityStudentsExtra; }
    public void setCapacityStudentsExtra(Integer capacityStudentsExtra) { this.capacityStudentsExtra = capacityStudentsExtra; }

    public Integer getCapacityStaff() { return capacityStaff; }
    public void setCapacityStaff(Integer capacityStaff) { this.capacityStaff = capacityStaff; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public Double getArea() { return area; }
    public void setArea(Double area) { this.area = area; }

    public Boolean getHasProjector() { return hasProjector; }
    public void setHasProjector(Boolean hasProjector) { this.hasProjector = hasProjector; }

    public String getScreenSize() { return screenSize; }
    public void setScreenSize(String screenSize) { this.screenSize = screenSize; }

    public Boolean getHasAc() { return hasAc; }
    public void setHasAc(Boolean hasAc) { this.hasAc = hasAc; }

    public Integer getPcCountManual() { return pcCountManual; }
    public void setPcCountManual(Integer pcCountManual) { this.pcCountManual = pcCountManual; }

    public Boolean getInternet() { return internet; }
    public void setInternet(Boolean internet) { this.internet = internet; }

    public String getSubnet() { return subnet; }
    public void setSubnet(String subnet) { this.subnet = subnet; }

    public Boolean getInSchedule() { return inSchedule; }
    public void setInSchedule(Boolean inSchedule) { this.inSchedule = inSchedule; }

    public String getScheduleLink() { return scheduleLink; }
    public void setScheduleLink(String scheduleLink) { this.scheduleLink = scheduleLink; }

    public String getActualScheduleLink() { return actualScheduleLink; }
    public void setActualScheduleLink(String actualScheduleLink) { this.actualScheduleLink = actualScheduleLink; }

    public String getEquipmentIssue() { return equipmentIssue; }
    public void setEquipmentIssue(String equipmentIssue) { this.equipmentIssue = equipmentIssue; }

    public BoardType getBoardType() { return boardType; }
    public void setBoardType(BoardType boardType) { this.boardType = boardType; }

    @Transient
    public Long getTotalCapacityStudents() {
        if (capacityStudents == null || capacityStudentsExtra == null) return null;
        return (long) capacityStudents + capacityStudentsExtra;
    }

    @AssertTrue(message = "Площадь должна быть конечным числом")
    public boolean isAreaFinite() { return area == null || Double.isFinite(area); }

    public Long getPcCountAuto() { return pcCountAuto; }
    public boolean getHasActiveProjector() { return hasActiveProjector; }
    public boolean getHasActivePrinter() { return hasActivePrinter; }

    public List<AuditoriumPhoto> getPhotos() { return photos; }

    /** Поддерживает обе стороны связи с AuditoriumPhoto. */
    public void setPhotos(List<AuditoriumPhoto> photos) {
        List<AuditoriumPhoto> replacement = photos == null ? new ArrayList<>() : new ArrayList<>(photos);
        for (AuditoriumPhoto item : new ArrayList<>(this.photos)) {
            removePhoto(item);
        }
        for (AuditoriumPhoto item : replacement) {
            addPhoto(item);
        }
    }

    public void addPhoto(AuditoriumPhoto item) {
        this.photos.add(item);
        item.setAuditorium(this);
    }

    public void removePhoto(AuditoriumPhoto item) {
        this.photos.remove(item);
    }

    public List<Projector> getProjectors() { return projectors; }

    /** Поддерживает обе стороны связи с Projector. */
    public void setProjectors(List<Projector> projectors) {
        List<Projector> replacement = projectors == null ? new ArrayList<>() : new ArrayList<>(projectors);
        for (Projector item : new ArrayList<>(this.projectors)) {
            removeProjector(item);
        }
        for (Projector item : replacement) {
            addProjector(item);
        }
    }

    public void addProjector(Projector item) {
        this.projectors.add(item);
        item.setAuditorium(this);
    }

    public void removeProjector(Projector item) {
        this.projectors.remove(item);
    }

    public List<Workstation> getWorkstations() { return workstations; }

    /** Поддерживает обе стороны связи с Workstation. */
    public void setWorkstations(List<Workstation> workstations) {
        List<Workstation> replacement = workstations == null ? new ArrayList<>() : new ArrayList<>(workstations);
        for (Workstation item : new ArrayList<>(this.workstations)) {
            removeWorkstation(item);
        }
        for (Workstation item : replacement) {
            addWorkstation(item);
        }
    }

    public void addWorkstation(Workstation item) {
        this.workstations.add(item);
        item.setAuditorium(this);
    }

    public void removeWorkstation(Workstation item) {
        this.workstations.remove(item);
    }

    public List<Printer> getPrinters() { return printers; }

    /** Поддерживает обе стороны связи с Printer. */
    public void setPrinters(List<Printer> printers) {
        List<Printer> replacement = printers == null ? new ArrayList<>() : new ArrayList<>(printers);
        for (Printer item : new ArrayList<>(this.printers)) {
            removePrinter(item);
        }
        for (Printer item : replacement) {
            addPrinter(item);
        }
    }

    public void addPrinter(Printer item) {
        this.printers.add(item);
        item.setAuditorium(this);
    }

    public void removePrinter(Printer item) {
        this.printers.remove(item);
    }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Set<AuditoriumAdditionalEquipment> getAdditionalEquipmentLinks() {
        return Collections.unmodifiableSet(additionalEquipmentLinks);
    }

    public void setAdditionalEquipment(Set<AdditionalEquipment> additionalEquipment) {
        Set<AdditionalEquipment> replacement = additionalEquipment == null ? new HashSet<>() : new HashSet<>(additionalEquipment);
        additionalEquipmentLinks.removeIf(link -> !replacement.contains(link.getAdditionalEquipment()));
        for (AdditionalEquipment type : replacement) {
            addAdditionalEquipment(type);
        }
    }

    public void addAdditionalEquipment(AdditionalEquipment type) {
        if (additionalEquipmentLinks.stream().noneMatch(link -> link.getAdditionalEquipment().equals(type))) {
            additionalEquipmentLinks.add(new AuditoriumAdditionalEquipment(this, type));
        }
    }

    public void removeAdditionalEquipment(AdditionalEquipment type) {
        additionalEquipmentLinks.removeIf(link -> link.getAdditionalEquipment().equals(type));
    }

    /** Сущности сравниваются по назначенному базой идентификатору. */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Auditorium other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Auditorium.class.hashCode();
    }
}
