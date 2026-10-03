package rut.iuct.auditoriums.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rut.iuct.auditoriums.dto.*;
import rut.iuct.auditoriums.exception.ApiException;
import rut.iuct.auditoriums.mapper.*;
import rut.iuct.auditoriums.model.*;
import rut.iuct.auditoriums.repository.*;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuditoriumService {
    private final AuditoriumRepository repository;
    private final DepartmentRepository departmentRepository;
    private final AuditoriumAdditionalEquipmentRepository links;
    private final PhotoService photos;
    private final WorkstationRepository workstations;
    private final ProjectorRepository projectors;
    private final PrinterRepository printers;
    private final AuditoriumMapper mapper;
    private final AdditionalEquipmentMapper equipmentMapper;
    private final AuditSupport audit;
    private final EntityManager entityManager;

    public AuditoriumService(AuditoriumRepository repository, DepartmentRepository departmentRepository,
            AuditoriumAdditionalEquipmentRepository links, PhotoService photos,
            WorkstationRepository workstations, ProjectorRepository projectors, PrinterRepository printers,
            AuditoriumMapper mapper, AdditionalEquipmentMapper equipmentMapper, AuditSupport audit, EntityManager entityManager) {
        this.repository = repository;
        this.departmentRepository = departmentRepository;
        this.links = links;
        this.photos = photos;
        this.workstations = workstations;
        this.projectors = projectors;
        this.printers = printers;
        this.mapper = mapper;
        this.equipmentMapper = equipmentMapper;
        this.audit = audit;
        this.entityManager = entityManager;
    }

    private Auditorium find(Long id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Аудитория не найдена"));
    }

    private void department(Auditorium entity, Long id) {
        entity.setDepartment(id == null ? null : departmentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Кафедра не найдена")));
    }

    private AuditoriumResponse response(Auditorium entity) {
        List<AdditionalEquipmentResponse> types = links.findSelectedTypes(entity.getId()).stream().map(equipmentMapper::toResponse).toList();
        return mapper.toResponse(entity, types);
    }

    public AuditoriumResponse get(Long id) { return response(find(id)); }

    @Transactional
    public AuditoriumResponse create(AuditoriumRequest request) {
        audit.validate(request, ValidationGroups.Create.class);
        Auditorium entity = new Auditorium();
        mapper.apply(request, entity);
        department(entity, request.getDepartmentId());
        audit.touch(entity, request.getChangedBy());
        repository.saveAndFlush(entity);
        entityManager.refresh(entity);
        return response(entity);
    }

    @Transactional
    public AuditoriumResponse update(Long id, AuditoriumRequest request) {
        audit.validate(request, ValidationGroups.Update.class);
        Auditorium entity = find(id);
        audit.checkVersion(entity, request.getVersion());
        mapper.apply(request, entity);
        department(entity, request.getDepartmentId());
        audit.touch(entity, request.getChangedBy());
        repository.flush();
        entityManager.refresh(entity);
        return response(entity);
    }

    @Transactional
    public void delete(Long id, String changedBy, Long version) {
        audit.validateDeletion(changedBy, version);
        Auditorium entity = find(id);
        audit.checkVersion(entity, version);
        if (workstations.existsByAuditorium_Id(id) || projectors.existsByAuditorium_Id(id) || printers.existsByAuditorium_Id(id)) {
            throw ApiException.conflict("В аудитории есть оборудование, в том числе списанное. Сначала удалите его отдельно");
        }
        photos.deleteForAuditorium(id);
        repository.delete(entity);
        repository.flush();
    }
}
