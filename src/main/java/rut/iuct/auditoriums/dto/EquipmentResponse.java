package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class EquipmentResponse extends AuditedResponse {

    private Long auditoriumId;

    private Long auditoriumVersion;

    private EquipmentStatus status;

    private LocalDateTime writtenOffAt;

    private String writtenOffBy;

    public Long getAuditoriumId() { return auditoriumId; }
    public void setAuditoriumId(Long auditoriumId) { this.auditoriumId = auditoriumId; }

    public Long getAuditoriumVersion() { return auditoriumVersion; }
    public void setAuditoriumVersion(Long auditoriumVersion) { this.auditoriumVersion = auditoriumVersion; }

    public EquipmentStatus getStatus() { return status; }
    public void setStatus(EquipmentStatus status) { this.status = status; }

    public LocalDateTime getWrittenOffAt() { return writtenOffAt; }
    public void setWrittenOffAt(LocalDateTime writtenOffAt) { this.writtenOffAt = writtenOffAt; }

    public String getWrittenOffBy() { return writtenOffBy; }
    public void setWrittenOffBy(String writtenOffBy) { this.writtenOffBy = writtenOffBy; }

}
