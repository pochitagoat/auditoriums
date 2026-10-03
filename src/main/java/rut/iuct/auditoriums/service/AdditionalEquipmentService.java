package rut.iuct.auditoriums.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rut.iuct.auditoriums.dto.*;
import rut.iuct.auditoriums.model.AdditionalEquipment;
import rut.iuct.auditoriums.mapper.AdditionalEquipmentMapper;
import rut.iuct.auditoriums.repository.*;
import rut.iuct.auditoriums.exception.ApiException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdditionalEquipmentService {
    private final AdditionalEquipmentRepository repository;
    private final AuditoriumAdditionalEquipmentRepository linkedRepository;
    private final AdditionalEquipmentMapper mapper;
    private final AuditSupport audit;

    public AdditionalEquipmentService(AdditionalEquipmentRepository repository, AuditoriumAdditionalEquipmentRepository linkedRepository, AdditionalEquipmentMapper mapper, AuditSupport audit) {
        this.repository = repository;
        this.linkedRepository = linkedRepository;
        this.mapper = mapper;
        this.audit = audit;
    }

    public List<AdditionalEquipmentResponse> list() {
        return repository.findAllByOrderByNameAscIdAsc().stream().map(mapper::toResponse).toList();
    }

    public AdditionalEquipmentResponse get(Long id) { return mapper.toResponse(find(id)); }

    private AdditionalEquipment find(Long id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Тип дополнительного оборудования не найден"));
    }

    @Transactional
    public AdditionalEquipmentResponse create(AdditionalEquipmentRequest request) {
        audit.validate(request, ValidationGroups.Create.class);
        AdditionalEquipment entity = new AdditionalEquipment();
        mapper.apply(request, entity);
        audit.touch(entity, request.getChangedBy());
        return mapper.toResponse(repository.saveAndFlush(entity));
    }

    @Transactional
    public AdditionalEquipmentResponse update(Long id, AdditionalEquipmentRequest request) {
        audit.validate(request, ValidationGroups.Update.class);
        AdditionalEquipment entity = find(id);
        audit.checkVersion(entity, request.getVersion());
        mapper.apply(request, entity);
        audit.touch(entity, request.getChangedBy());
        repository.flush();
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id, String changedBy, Long version) {
        audit.validateDeletion(changedBy, version);
        AdditionalEquipment entity = find(id);
        audit.checkVersion(entity, version);
        if (linkedRepository.existsByAdditionalEquipment_Id(id)) {
            throw ApiException.conflict("Тип дополнительного оборудования используется аудиториями. Сначала освободите связи");
        }
        repository.delete(entity);
        repository.flush();
    }
}
