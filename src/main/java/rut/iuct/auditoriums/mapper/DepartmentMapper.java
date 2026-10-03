package rut.iuct.auditoriums.mapper;

import org.springframework.stereotype.Component;
import rut.iuct.auditoriums.model.Department;
import rut.iuct.auditoriums.dto.DepartmentRequest;
import rut.iuct.auditoriums.dto.DepartmentResponse;

@Component
public class DepartmentMapper {
    public DepartmentResponse toResponse(Department source) {
        if (source == null) return null;
        DepartmentResponse target = new DepartmentResponse();
        AuditMapper.copy(source, source.getId(), target);
        target.setName(source.getName());
        target.setMolLink(source.getMolLink());
        return target;
    }

    public void apply(DepartmentRequest source, Department target) {
        target.setName(source.getName());
        target.setMolLink(source.getMolLink());
    }
}
