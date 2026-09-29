package rut.iuct.auditoriums.model;

import jakarta.persistence.*;

/** Фотография аудитории. В базе хранится только относительный путь. */
@Entity
@Table(name = "auditorium_photo")
public class AuditoriumPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_path", nullable = false, length = 1024)
    private String filePath;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "content_type")
    private String contentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auditorium_id", nullable = false)
    private Auditorium auditorium;

    public AuditoriumPhoto() {
    }

    public AuditoriumPhoto(
            Long id,
            String filePath,
            String originalFilename,
            String contentType,
            Auditorium auditorium
    ) {
        this.id = id;
        this.filePath = filePath;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.auditorium = auditorium;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Auditorium getAuditorium() { return auditorium; }
    public void setAuditorium(Auditorium auditorium) { this.auditorium = auditorium; }

    /** Сущности сравниваются по назначенному базой идентификатору. */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof AuditoriumPhoto other)) return false;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return AuditoriumPhoto.class.hashCode();
    }
}
