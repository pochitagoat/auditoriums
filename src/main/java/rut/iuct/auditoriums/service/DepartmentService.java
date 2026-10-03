package rut.iuct.auditoriums.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rut.iuct.auditoriums.dto.*;
import rut.iuct.auditoriums.model.Department;
import rut.iuct.auditoriums.mapper.DepartmentMapper;
import rut.iuct.auditoriums.repository.*;
import rut.iuct.auditoriums.exception.ApiException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DepartmentService {
    private final DepartmentRepository repository;
    private final AuditoriumRepository linkedRepository;
    private final DepartmentMapper mapper;
    private final AuditSupport audit;

    public DepartmentService(DepartmentRepository repository, AuditoriumRepository linkedRepository, DepartmentMapper mapper, AuditSupport audit) {
        this.repository = repository;
        this.linkedRepository = linkedRepository;
        this.mapper = mapper;
        this.audit = audit;
    }

    public List<DepartmentResponse> list() {
        return repository.findAllByOrderByNameAscIdAsc().stream().map(mapper::toResponse).toList();
    }

    public DepartmentResponse get(Long id) { return mapper.toResponse(find(id)); }

    private Department find(Long id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Кафедра не найдена"));
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        audit.validate(request, ValidationGroups.Create.class);
        Department entity = new Department();
        mapper.apply(request, entity);
        audit.touch(entity, request.getChangedBy());
        return mapper.toResponse(repository.saveAndFlush(entity));
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        audit.validate(request, ValidationGroups.Update.class);
        Department entity = find(id);
        audit.checkVersion(entity, request.getVersion());
        mapper.apply(request, entity);
        audit.touch(entity, request.getChangedBy());
        repository.flush();
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id, String changedBy, Long version) {
        audit.validateDeletion(changedBy, version);
        Department entity = find(id);
        audit.checkVersion(entity, version);
        if (linkedRepository.existsByDepartment_Id(id)) {
            throw ApiException.conflict("Кафедра используется аудиториями. Сначала освободите связи");
        }
        repository.delete(entity);
        repository.flush();
    }
}
