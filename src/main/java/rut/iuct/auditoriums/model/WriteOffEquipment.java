package rut.iuct.auditoriums.model;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** Поля списания только рабочих мест, проекторов и принтеров. */
@MappedSuperclass
public abstract class WriteOffEquipment extends AuditedEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private EquipmentStatus status = EquipmentStatus.ACTIVE;

    @Column(name = "written_off_at")
    private LocalDateTime writtenOffAt;

    @Size(max = 255)
    @Column(name = "written_off_by", length = 255)
    private String writtenOffBy;

    public EquipmentStatus getStatus() { return status; }
    public void setStatus(EquipmentStatus status) { this.status = status; }

    public LocalDateTime getWrittenOffAt() { return writtenOffAt; }
    public void setWrittenOffAt(LocalDateTime writtenOffAt) { this.writtenOffAt = writtenOffAt; }

    public String getWrittenOffBy() { return writtenOffBy; }
    public void setWrittenOffBy(String writtenOffBy) { this.writtenOffBy = writtenOffBy; }

    @AssertTrue(message = "Статус оборудования должен соответствовать дате и ФИО списания")
    public boolean isWriteOffStateValid() {
        return (status == EquipmentStatus.ACTIVE && writtenOffAt == null && writtenOffBy == null)
                || (status == EquipmentStatus.WRITTEN_OFF && writtenOffAt != null
                && writtenOffBy != null && !writtenOffBy.isBlank());
    }
}
