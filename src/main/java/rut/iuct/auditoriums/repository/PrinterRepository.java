package rut.iuct.auditoriums.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rut.iuct.auditoriums.model.Printer;
import rut.iuct.auditoriums.model.EquipmentStatus;
import java.util.List;

public interface PrinterRepository extends JpaRepository<Printer, Long> {
    List<Printer> findByAuditorium_IdOrderByIdAsc(Long auditoriumId);
    List<Printer> findByAuditorium_IdAndStatusOrderByIdAsc(Long auditoriumId, EquipmentStatus status);
    boolean existsByAuditorium_Id(Long auditoriumId);
}
