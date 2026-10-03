package rut.iuct.auditoriums.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rut.iuct.auditoriums.model.Workstation;
import rut.iuct.auditoriums.model.EquipmentStatus;
import java.util.List;

public interface WorkstationRepository extends JpaRepository<Workstation, Long> {
    List<Workstation> findByAuditorium_IdOrderByIdAsc(Long auditoriumId);
    List<Workstation> findByAuditorium_IdAndStatusOrderByIdAsc(Long auditoriumId, EquipmentStatus status);
    boolean existsByAuditorium_Id(Long auditoriumId);
}
