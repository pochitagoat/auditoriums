package rut.iuct.auditoriums.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

/** Выбор справочного типа для аудитории, без статуса и собственного аудита. */
@Entity
@Table(name = "auditorium_additional_equipment")
public class AuditoriumAdditionalEquipment {

    @EmbeddedId
    private AuditoriumAdditionalEquipmentId id = new AuditoriumAdditionalEquipmentId();

    @NotNull
    @MapsId("auditoriumId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auditorium_id", nullable = false)
    private Auditorium auditorium;

    @NotNull
    @MapsId("additionalEquipmentId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "additional_equipment_id", nullable = false)
    private AdditionalEquipment additionalEquipment;

    protected AuditoriumAdditionalEquipment() {
    }

    public AuditoriumAdditionalEquipment(Auditorium auditorium, AdditionalEquipment additionalEquipment) {
        this.auditorium = Objects.requireNonNull(auditorium);
        this.additionalEquipment = Objects.requireNonNull(additionalEquipment);
        this.id = new AuditoriumAdditionalEquipmentId(auditorium.getId(), additionalEquipment.getId());
    }

    public AuditoriumAdditionalEquipmentId getId() { return id; }
    public Auditorium getAuditorium() { return auditorium; }
    public AdditionalEquipment getAdditionalEquipment() { return additionalEquipment; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof AuditoriumAdditionalEquipment other)) return false;
        return id.getAuditoriumId() != null && id.getAdditionalEquipmentId() != null
                && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return AuditoriumAdditionalEquipment.class.hashCode();
    }
}
