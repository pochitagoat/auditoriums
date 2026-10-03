package rut.iuct.auditoriums.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rut.iuct.auditoriums.dto.*;
import rut.iuct.auditoriums.mapper.AuditoriumMapper;
import rut.iuct.auditoriums.model.Auditorium;
import rut.iuct.auditoriums.repository.AuditoriumRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AuditoriumSearchService {
    private final AuditoriumRepository repository;
    private final AuditoriumMapper mapper;
    private final AuditSupport validation;

    public AuditoriumSearchService(AuditoriumRepository repository, AuditoriumMapper mapper, AuditSupport validation) {
        this.repository = repository;
        this.mapper = mapper;
        this.validation = validation;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditoriumListItem> search(AuditoriumFilter filter) {
        validation.validate(filter);
        Specification<Auditorium> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.getQ() != null) {
                String escaped = filter.getQ().toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
                predicates.add(builder.like(builder.lower(root.get("name")), "%" + escaped + "%", '\\'));
            }
            if (filter.getType() != null) predicates.add(builder.equal(root.get("type"), filter.getType()));
            if (filter.getBuilding() != null) predicates.add(builder.equal(root.get("building"), filter.getBuilding()));
            if (filter.getFloor() != null) predicates.add(builder.equal(root.get("floor"), filter.getFloor()));
            if (filter.getBoardType() != null) predicates.add(builder.equal(root.get("boardType"), filter.getBoardType()));
            if (filter.getDepartmentId() != null) predicates.add(builder.equal(root.get("department").get("id"), filter.getDepartmentId()));
            if (filter.getInternet() != null) predicates.add(builder.equal(root.get("internet"), filter.getInternet()));
            if (filter.getHasAc() != null) predicates.add(builder.equal(root.get("hasAc"), filter.getHasAc()));
            if (filter.getHasProjector() != null) predicates.add(builder.equal(root.get("hasActiveProjector"), filter.getHasProjector()));
            if (filter.getHasPrinter() != null) predicates.add(builder.equal(root.get("hasActivePrinter"), filter.getHasPrinter()));
            if (filter.getHasWorkstations() != null) {
                Predicate present = builder.greaterThan(root.get("pcCountAuto"), 0L);
                predicates.add(filter.getHasWorkstations() ? present : builder.not(present));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        // В PostgreSQL ASC размещает NULL после известных корпусов.
        var page = repository.findAll(specification, PageRequest.of(filter.getPage(), filter.getSize(), Sort.by("building", "name", "id")));
        PageResponse<AuditoriumListItem> result = new PageResponse<>();
        result.setContent(page.getContent().stream().map(mapper::toListItem).toList());
        result.setPage(page.getNumber());
        result.setSize(page.getSize());
        result.setTotalElements(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        return result;
    }
}
