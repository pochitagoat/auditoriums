package rut.iuct.auditoriums.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rut.iuct.auditoriums.model.Projector;
import rut.iuct.auditoriums.model.EquipmentStatus;
import java.util.List;

public interface ProjectorRepository extends JpaRepository<Projector, Long> {
    List<Projector> findByAuditorium_IdOrderByIdAsc(Long auditoriumId);
    List<Projector> findByAuditorium_IdAndStatusOrderByIdAsc(Long auditoriumId, EquipmentStatus status);
    boolean existsByAuditorium_Id(Long auditoriumId);
}
