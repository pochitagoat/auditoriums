package rut.iuct.auditoriums.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** Общие поля аудита и защита от одновременной перезаписи. */
@MappedSuperclass
public abstract class AuditedEntity {

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    @Size(max = 255)
    @Column(name = "changed_by", length = 255)
    private String changedBy;

    @Version
    @PositiveOrZero
    @Column(name = "version", nullable = false)
    private Long version;

    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public Long getVersion() { return version; }
}
