package rut.iuct.auditoriums.model;

import jakarta.persistence.*;

/** Подразделение, связанное с аудиториями. */
@Entity
@Table(name = "department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "mol_link", length = 2048)
    private String molLink;

    public Department() {
    }

    public Department(
            Long id,
            String name,
            String molLink
    ) {
        this.id = id;
        this.name = name;
        this.molLink = molLink;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMolLink() { return molLink; }
    public void setMolLink(String molLink) { this.molLink = molLink; }

    /** Сущности сравниваются по назначенному базой идентификатору. */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Department other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Department.class.hashCode();
    }
}
