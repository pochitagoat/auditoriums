package rut.iuct.auditoriums.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rut.iuct.auditoriums.dto.*;
import rut.iuct.auditoriums.exception.ApiException;
import rut.iuct.auditoriums.mapper.AdditionalEquipmentMapper;
import rut.iuct.auditoriums.model.*;
import rut.iuct.auditoriums.repository.*;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdditionalEquipmentLinkService {
    private final AuditoriumRepository auditoriums;
    private final AdditionalEquipmentRepository types;
    private final AuditoriumAdditionalEquipmentRepository links;
    private final AdditionalEquipmentMapper mapper;
    private final AuditSupport audit;

    public AdditionalEquipmentLinkService(AuditoriumRepository auditoriums, AdditionalEquipmentRepository types,
            AuditoriumAdditionalEquipmentRepository links, AdditionalEquipmentMapper mapper, AuditSupport audit) {
        this.auditoriums = auditoriums;
        this.types = types;
        this.links = links;
        this.mapper = mapper;
        this.audit = audit;
    }

    private Auditorium auditorium(Long id) {
        return auditoriums.findById(id).orElseThrow(() -> ApiException.notFound("Аудитория не найдена"));
    }

    public List<AdditionalEquipmentResponse> list(Long auditoriumId) {
        auditorium(auditoriumId);
        return links.findSelectedTypes(auditoriumId).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public AdditionalEquipmentResponse add(Long auditoriumId, EquipmentLinkRequest request) {
        audit.validate(request);
        Auditorium parent = auditorium(auditoriumId);
        audit.checkVersion(parent, request.getAuditoriumVersion());
        AdditionalEquipment type = types.findById(request.getAdditionalEquipmentId())
                .orElseThrow(() -> ApiException.notFound("Тип дополнительного оборудования не найден"));
        if (links.existsById(new AuditoriumAdditionalEquipmentId(auditoriumId, type.getId()))) {
            throw ApiException.conflict("Этот тип оборудования уже выбран для аудитории");
        }
        parent.addAdditionalEquipment(type);
        audit.touch(parent, request.getChangedBy());
        auditoriums.flush();
        return mapper.toResponse(type);
    }

    @Transactional
    public void remove(Long auditoriumId, Long additionalEquipmentId, String changedBy, Long version) {
        audit.validateDeletion(changedBy, version);
        Auditorium parent = auditorium(auditoriumId);
        audit.checkVersion(parent, version);
        AuditoriumAdditionalEquipment link = links.findById(new AuditoriumAdditionalEquipmentId(auditoriumId, additionalEquipmentId))
                .orElseThrow(() -> ApiException.notFound("Связь с типом оборудования не найдена"));
        parent.removeAdditionalEquipment(link.getAdditionalEquipment());
        audit.touch(parent, changedBy);
        auditoriums.flush();
    }
}
