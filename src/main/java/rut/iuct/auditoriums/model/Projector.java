package rut.iuct.auditoriums.model;

import jakarta.persistence.*;

/** Проектор, закреплённый за аудиторией. */
@Entity
@Table(name = "projector")
public class Projector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model")
    private String model;

    @Column(name = "mol")
    private String mol;

    @Column(name = "warehouse")
    private String warehouse;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auditorium_id", nullable = false)
    private Auditorium auditorium;

    public Projector() {
    }

    public Projector(
            Long id,
            String model,
            String mol,
            String warehouse,
            Auditorium auditorium
    ) {
        this.id = id;
        this.model = model;
        this.mol = mol;
        this.warehouse = warehouse;
        this.auditorium = auditorium;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getMol() { return mol; }
    public void setMol(String mol) { this.mol = mol; }

    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }

    public Auditorium getAuditorium() { return auditorium; }
    public void setAuditorium(Auditorium auditorium) { this.auditorium = auditorium; }

    /** Сущности сравниваются по назначенному базой идентификатору. */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Projector other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Projector.class.hashCode();
    }
}
