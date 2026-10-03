package rut.iuct.auditoriums.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rut.iuct.auditoriums.model.AdditionalEquipment;
import java.util.List;

public interface AdditionalEquipmentRepository extends JpaRepository<AdditionalEquipment, Long> {
    List<AdditionalEquipment> findAllByOrderByNameAscIdAsc();
}
