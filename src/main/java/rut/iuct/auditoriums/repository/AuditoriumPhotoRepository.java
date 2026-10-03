package rut.iuct.auditoriums.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rut.iuct.auditoriums.model.AuditoriumPhoto;
import java.util.List;

public interface AuditoriumPhotoRepository extends JpaRepository<AuditoriumPhoto, Long> {
    boolean existsByAuditorium_Id(Long auditoriumId);
    List<AuditoriumPhoto> findByAuditorium_IdOrderByIdAsc(Long auditoriumId);
}
