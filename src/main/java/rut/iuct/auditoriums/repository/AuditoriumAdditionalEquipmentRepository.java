package rut.iuct.auditoriums.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rut.iuct.auditoriums.model.AdditionalEquipment;
import rut.iuct.auditoriums.model.AuditoriumAdditionalEquipment;
import rut.iuct.auditoriums.model.AuditoriumAdditionalEquipmentId;
import java.util.List;

public interface AuditoriumAdditionalEquipmentRepository extends JpaRepository<AuditoriumAdditionalEquipment, AuditoriumAdditionalEquipmentId> {
    boolean existsByAdditionalEquipment_Id(Long additionalEquipmentId);

    @Query("select l.additionalEquipment from AuditoriumAdditionalEquipment l where l.auditorium.id = :auditoriumId order by l.additionalEquipment.name, l.additionalEquipment.id")
    List<AdditionalEquipment> findSelectedTypes(Long auditoriumId);
}
