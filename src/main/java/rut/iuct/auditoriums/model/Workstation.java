package rut.iuct.auditoriums.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Рабочее место или ПК в аудитории. */
@Entity
@Table(name = "workstation")
public class Workstation extends WriteOffEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 255)
    @Column(name = "pc_name")
    private String pcName;

    @Size(max = 255)
    @Column(name = "monitors")
    private String monitors;

    @Size(max = 255)
    @Column(name = "keyboards")
    private String keyboards;

    @Size(max = 255)
    @Column(name = "mice")
    private String mice;

    @Size(max = 255)
    @Column(name = "location_info")
    private String locationInfo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auditorium_id", nullable = false)
    private Auditorium auditorium;

    public Workstation() {
    }

    public Workstation(
            Long id,
            String pcName,
            String monitors,
            String keyboards,
            String mice,
            String locationInfo,
            Auditorium auditorium
    ) {
        this.id = id;
        this.pcName = pcName;
        this.monitors = monitors;
        this.keyboards = keyboards;
        this.mice = mice;
        this.locationInfo = locationInfo;
        this.auditorium = auditorium;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPcName() { return pcName; }
    public void setPcName(String pcName) { this.pcName = pcName; }

    public String getMonitors() { return monitors; }
    public void setMonitors(String monitors) { this.monitors = monitors; }

    public String getKeyboards() { return keyboards; }
    public void setKeyboards(String keyboards) { this.keyboards = keyboards; }

    public String getMice() { return mice; }
    public void setMice(String mice) { this.mice = mice; }

    public String getLocationInfo() { return locationInfo; }
    public void setLocationInfo(String locationInfo) { this.locationInfo = locationInfo; }

    public Auditorium getAuditorium() { return auditorium; }
    public void setAuditorium(Auditorium auditorium) { this.auditorium = auditorium; }

    /** Сущности сравниваются по назначенному базой идентификатору. */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Workstation other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Workstation.class.hashCode();
    }
}
