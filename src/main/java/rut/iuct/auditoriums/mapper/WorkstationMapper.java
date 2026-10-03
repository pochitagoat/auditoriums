package rut.iuct.auditoriums.mapper;

import org.springframework.stereotype.Component;
import rut.iuct.auditoriums.model.Workstation;
import rut.iuct.auditoriums.dto.WorkstationRequest;
import rut.iuct.auditoriums.dto.WorkstationResponse;

@Component
public class WorkstationMapper {
    public WorkstationResponse toResponse(Workstation source) {
        WorkstationResponse target = new WorkstationResponse();
        AuditMapper.copy(source, source.getId(), target);
        target.setAuditoriumId(source.getAuditorium().getId());
        target.setAuditoriumVersion(source.getAuditorium().getVersion());
        target.setStatus(source.getStatus());
        target.setWrittenOffAt(source.getWrittenOffAt());
        target.setWrittenOffBy(source.getWrittenOffBy());
        target.setPcName(source.getPcName());
        target.setMonitors(source.getMonitors());
        target.setKeyboards(source.getKeyboards());
        target.setMice(source.getMice());
        target.setLocationInfo(source.getLocationInfo());
        return target;
    }

    public void apply(WorkstationRequest source, Workstation target) {
        target.setPcName(source.getPcName());
        target.setMonitors(source.getMonitors());
        target.setKeyboards(source.getKeyboards());
        target.setMice(source.getMice());
        target.setLocationInfo(source.getLocationInfo());
    }
}
