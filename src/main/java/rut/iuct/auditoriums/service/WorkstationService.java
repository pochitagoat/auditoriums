package rut.iuct.auditoriums.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rut.iuct.auditoriums.dto.*;
import rut.iuct.auditoriums.exception.ApiException;
import rut.iuct.auditoriums.model.*;
import rut.iuct.auditoriums.mapper.WorkstationMapper;
import rut.iuct.auditoriums.repository.*;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class WorkstationService {
    private final WorkstationRepository repository;
    private final AuditoriumRepository auditoriumRepository;
    private final WorkstationMapper mapper;
    private final AuditSupport audit;

    public WorkstationService(WorkstationRepository repository, AuditoriumRepository auditoriumRepository, WorkstationMapper mapper, AuditSupport audit) {
        this.repository = repository;
        this.auditoriumRepository = auditoriumRepository;
        this.mapper = mapper;
        this.audit = audit;
    }

    private Workstation find(Long id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Рабочее место не найдено"));
    }

    private Auditorium auditorium(Long id) {
        return auditoriumRepository.findById(id).orElseThrow(() -> ApiException.notFound("Аудитория не найдена"));
    }

    public List<WorkstationResponse> list(Long auditoriumId, boolean includeWrittenOff) {
        auditorium(auditoriumId);
        List<Workstation> items = includeWrittenOff
                ? repository.findByAuditorium_IdOrderByIdAsc(auditoriumId)
                : repository.findByAuditorium_IdAndStatusOrderByIdAsc(auditoriumId, EquipmentStatus.ACTIVE);
        return items.stream().map(mapper::toResponse).toList();
    }

    public WorkstationResponse get(Long id) { return mapper.toResponse(find(id)); }

    @Transactional
    public WorkstationResponse create(Long auditoriumId, WorkstationRequest request) {
        audit.validate(request, ValidationGroups.Create.class);
        Auditorium parent = auditorium(auditoriumId);
        audit.checkVersion(parent, request.getAuditoriumVersion());
        Workstation entity = new Workstation();
        entity.setAuditorium(parent);
        mapper.apply(request, entity);
        audit.touch(entity, request.getChangedBy());
        audit.touch(parent, request.getChangedBy());
        repository.saveAndFlush(entity);
        return mapper.toResponse(entity);
    }

    @Transactional
    public WorkstationResponse update(Long id, WorkstationRequest request) {
        audit.validate(request, ValidationGroups.Update.class);
        Workstation entity = find(id);
        audit.checkVersion(entity, request.getVersion());
        mapper.apply(request, entity);
        audit.touch(entity, request.getChangedBy());
        audit.touch(entity.getAuditorium(), request.getChangedBy());
        repository.flush();
        return mapper.toResponse(entity);
    }

    @Transactional
    public WorkstationResponse changeStatus(Long id, EquipmentStatus desired, StatusChangeRequest request) {
        Workstation entity = find(id);
        if (audit.changeStatus(entity, desired, request)) {
            audit.touch(entity.getAuditorium(), request.getChangedBy());
            repository.flush();
        }
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id, String changedBy, Long version) {
        audit.validateDeletion(changedBy, version);
        Workstation entity = find(id);
        audit.checkVersion(entity, version);
        audit.touch(entity.getAuditorium(), changedBy);
        repository.delete(entity);
        repository.flush();
    }
}
