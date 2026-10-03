package rut.iuct.auditoriums.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rut.iuct.auditoriums.dto.*;
import rut.iuct.auditoriums.exception.ApiException;
import rut.iuct.auditoriums.model.*;
import rut.iuct.auditoriums.mapper.ProjectorMapper;
import rut.iuct.auditoriums.repository.*;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectorService {
    private final ProjectorRepository repository;
    private final AuditoriumRepository auditoriumRepository;
    private final ProjectorMapper mapper;
    private final AuditSupport audit;

    public ProjectorService(ProjectorRepository repository, AuditoriumRepository auditoriumRepository, ProjectorMapper mapper, AuditSupport audit) {
        this.repository = repository;
        this.auditoriumRepository = auditoriumRepository;
        this.mapper = mapper;
        this.audit = audit;
    }

    private Projector find(Long id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Проектор не найден"));
    }

    private Auditorium auditorium(Long id) {
        return auditoriumRepository.findById(id).orElseThrow(() -> ApiException.notFound("Аудитория не найдена"));
    }

    public List<ProjectorResponse> list(Long auditoriumId, boolean includeWrittenOff) {
        auditorium(auditoriumId);
        List<Projector> items = includeWrittenOff
                ? repository.findByAuditorium_IdOrderByIdAsc(auditoriumId)
                : repository.findByAuditorium_IdAndStatusOrderByIdAsc(auditoriumId, EquipmentStatus.ACTIVE);
        return items.stream().map(mapper::toResponse).toList();
    }

    public ProjectorResponse get(Long id) { return mapper.toResponse(find(id)); }

    @Transactional
    public ProjectorResponse create(Long auditoriumId, ProjectorRequest request) {
        audit.validate(request, ValidationGroups.Create.class);
        Auditorium parent = auditorium(auditoriumId);
        audit.checkVersion(parent, request.getAuditoriumVersion());
        Projector entity = new Projector();
        entity.setAuditorium(parent);
        mapper.apply(request, entity);
        audit.touch(entity, request.getChangedBy());
        audit.touch(parent, request.getChangedBy());
        repository.saveAndFlush(entity);
        return mapper.toResponse(entity);
    }

    @Transactional
    public ProjectorResponse update(Long id, ProjectorRequest request) {
        audit.validate(request, ValidationGroups.Update.class);
        Projector entity = find(id);
        audit.checkVersion(entity, request.getVersion());
        mapper.apply(request, entity);
        audit.touch(entity, request.getChangedBy());
        audit.touch(entity.getAuditorium(), request.getChangedBy());
        repository.flush();
        return mapper.toResponse(entity);
    }

    @Transactional
    public ProjectorResponse changeStatus(Long id, EquipmentStatus desired, StatusChangeRequest request) {
        Projector entity = find(id);
        if (audit.changeStatus(entity, desired, request)) {
            audit.touch(entity.getAuditorium(), request.getChangedBy());
            repository.flush();
        }
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id, String changedBy, Long version) {
        audit.validateDeletion(changedBy, version);
        Projector entity = find(id);
        audit.checkVersion(entity, version);
        audit.touch(entity.getAuditorium(), changedBy);
        repository.delete(entity);
        repository.flush();
    }
}
