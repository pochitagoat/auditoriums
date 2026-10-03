package rut.iuct.auditoriums.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AuditoriumAdditionalEquipmentId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "auditorium_id", nullable = false)
    private Long auditoriumId;

    @Column(name = "additional_equipment_id", nullable = false)
    private Long additionalEquipmentId;

    public AuditoriumAdditionalEquipmentId() {
    }

    public AuditoriumAdditionalEquipmentId(Long auditoriumId, Long additionalEquipmentId) {
        this.auditoriumId = auditoriumId;
        this.additionalEquipmentId = additionalEquipmentId;
    }

    public Long getAuditoriumId() { return auditoriumId; }
    public void setAuditoriumId(Long auditoriumId) { this.auditoriumId = auditoriumId; }

    public Long getAdditionalEquipmentId() { return additionalEquipmentId; }
    public void setAdditionalEquipmentId(Long additionalEquipmentId) { this.additionalEquipmentId = additionalEquipmentId; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof AuditoriumAdditionalEquipmentId other)) return false;
        return Objects.equals(auditoriumId, other.auditoriumId)
                && Objects.equals(additionalEquipmentId, other.additionalEquipmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(auditoriumId, additionalEquipmentId);
    }
}
