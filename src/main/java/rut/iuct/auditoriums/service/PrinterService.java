package rut.iuct.auditoriums.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rut.iuct.auditoriums.dto.*;
import rut.iuct.auditoriums.exception.ApiException;
import rut.iuct.auditoriums.model.*;
import rut.iuct.auditoriums.mapper.PrinterMapper;
import rut.iuct.auditoriums.repository.*;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class PrinterService {
    private final PrinterRepository repository;
    private final AuditoriumRepository auditoriumRepository;
    private final PrinterMapper mapper;
    private final AuditSupport audit;

    public PrinterService(PrinterRepository repository, AuditoriumRepository auditoriumRepository, PrinterMapper mapper, AuditSupport audit) {
        this.repository = repository;
        this.auditoriumRepository = auditoriumRepository;
        this.mapper = mapper;
        this.audit = audit;
    }

    private Printer find(Long id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("Принтер не найден"));
    }

    private Auditorium auditorium(Long id) {
        return auditoriumRepository.findById(id).orElseThrow(() -> ApiException.notFound("Аудитория не найдена"));
    }

    public List<PrinterResponse> list(Long auditoriumId, boolean includeWrittenOff) {
        auditorium(auditoriumId);
        List<Printer> items = includeWrittenOff
                ? repository.findByAuditorium_IdOrderByIdAsc(auditoriumId)
                : repository.findByAuditorium_IdAndStatusOrderByIdAsc(auditoriumId, EquipmentStatus.ACTIVE);
        return items.stream().map(mapper::toResponse).toList();
    }

    public PrinterResponse get(Long id) { return mapper.toResponse(find(id)); }

    @Transactional
    public PrinterResponse create(Long auditoriumId, PrinterRequest request) {
        audit.validate(request, ValidationGroups.Create.class);
        Auditorium parent = auditorium(auditoriumId);
        audit.checkVersion(parent, request.getAuditoriumVersion());
        Printer entity = new Printer();
        entity.setAuditorium(parent);
        mapper.apply(request, entity);
        audit.touch(entity, request.getChangedBy());
        audit.touch(parent, request.getChangedBy());
        repository.saveAndFlush(entity);
        return mapper.toResponse(entity);
    }

    @Transactional
    public PrinterResponse update(Long id, PrinterRequest request) {
        audit.validate(request, ValidationGroups.Update.class);
        Printer entity = find(id);
        audit.checkVersion(entity, request.getVersion());
        mapper.apply(request, entity);
        audit.touch(entity, request.getChangedBy());
        audit.touch(entity.getAuditorium(), request.getChangedBy());
        repository.flush();
        return mapper.toResponse(entity);
    }

    @Transactional
    public PrinterResponse changeStatus(Long id, EquipmentStatus desired, StatusChangeRequest request) {
        Printer entity = find(id);
        if (audit.changeStatus(entity, desired, request)) {
            audit.touch(entity.getAuditorium(), request.getChangedBy());
            repository.flush();
        }
        return mapper.toResponse(entity);
    }

    @Transactional
    public void delete(Long id, String changedBy, Long version) {
        audit.validateDeletion(changedBy, version);
        Printer entity = find(id);
        audit.checkVersion(entity, version);
        audit.touch(entity.getAuditorium(), changedBy);
        repository.delete(entity);
        repository.flush();
    }
}
