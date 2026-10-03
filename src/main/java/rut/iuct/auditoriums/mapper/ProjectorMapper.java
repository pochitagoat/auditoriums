package rut.iuct.auditoriums.mapper;

import org.springframework.stereotype.Component;
import rut.iuct.auditoriums.model.Projector;
import rut.iuct.auditoriums.dto.ProjectorRequest;
import rut.iuct.auditoriums.dto.ProjectorResponse;

@Component
public class ProjectorMapper {
    public ProjectorResponse toResponse(Projector source) {
        ProjectorResponse target = new ProjectorResponse();
        AuditMapper.copy(source, source.getId(), target);
        target.setAuditoriumId(source.getAuditorium().getId());
        target.setAuditoriumVersion(source.getAuditorium().getVersion());
        target.setStatus(source.getStatus());
        target.setWrittenOffAt(source.getWrittenOffAt());
        target.setWrittenOffBy(source.getWrittenOffBy());
        target.setModel(source.getModel());
        target.setMol(source.getMol());
        target.setWarehouse(source.getWarehouse());
        return target;
    }

    public void apply(ProjectorRequest source, Projector target) {
        target.setModel(source.getModel());
        target.setMol(source.getMol());
        target.setWarehouse(source.getWarehouse());
    }
}
