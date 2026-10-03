package rut.iuct.auditoriums.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Печатающее устройство в аудитории. */
@Entity
@Table(name = "printer")
public class Printer extends WriteOffEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 255)
    @Column(name = "model")
    private String model;

    @Size(max = 255)
    @Column(name = "warehouse")
    private String warehouse;

    @Size(max = 2048)
    @Column(name = "cartridge_link", length = 2048)
    private String cartridgeLink;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auditorium_id", nullable = false)
    private Auditorium auditorium;

    public Printer() {
    }

    public Printer(
            Long id,
            String model,
            String warehouse,
            String cartridgeLink,
            Auditorium auditorium
    ) {
        this.id = id;
        this.model = model;
        this.warehouse = warehouse;
        this.cartridgeLink = cartridgeLink;
        this.auditorium = auditorium;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getWarehouse() { return warehouse; }
    public void setWarehouse(String warehouse) { this.warehouse = warehouse; }

    public String getCartridgeLink() { return cartridgeLink; }
    public void setCartridgeLink(String cartridgeLink) { this.cartridgeLink = cartridgeLink; }

    public Auditorium getAuditorium() { return auditorium; }
    public void setAuditorium(Auditorium auditorium) { this.auditorium = auditorium; }

    /** Сущности сравниваются по назначенному базой идентификатору. */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Printer other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Printer.class.hashCode();
    }
}
