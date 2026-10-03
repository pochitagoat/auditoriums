package rut.iuct.auditoriums.mapper;

import org.springframework.stereotype.Component;
import rut.iuct.auditoriums.model.AdditionalEquipment;
import rut.iuct.auditoriums.dto.AdditionalEquipmentRequest;
import rut.iuct.auditoriums.dto.AdditionalEquipmentResponse;

@Component
public class AdditionalEquipmentMapper {
    public AdditionalEquipmentResponse toResponse(AdditionalEquipment source) {
        if (source == null) return null;
        AdditionalEquipmentResponse target = new AdditionalEquipmentResponse();
        AuditMapper.copy(source, source.getId(), target);
        target.setName(source.getName());
        return target;
    }

    public void apply(AdditionalEquipmentRequest source, AdditionalEquipment target) {
        target.setName(source.getName());
    }
}
