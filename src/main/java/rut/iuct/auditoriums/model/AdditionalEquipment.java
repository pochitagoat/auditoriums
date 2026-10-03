package rut.iuct.auditoriums.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Вид дополнительного оборудования. */
@Entity
@Table(name = "additional_equipment")
public class AdditionalEquipment extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;

    public AdditionalEquipment() {
    }

    public AdditionalEquipment(
            Long id,
            String name
    ) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** Сущности сравниваются по назначенному базой идентификатору. */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof AdditionalEquipment other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return AdditionalEquipment.class.hashCode();
    }
}
