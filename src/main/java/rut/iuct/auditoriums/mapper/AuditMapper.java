package rut.iuct.auditoriums.mapper;

import rut.iuct.auditoriums.dto.AuditedResponse;
import rut.iuct.auditoriums.model.AuditedEntity;

public final class AuditMapper {
    private AuditMapper() {
    }

    public static void copy(AuditedEntity source, Long id, AuditedResponse target) {
        target.setId(id);
        target.setVersion(source.getVersion());
        target.setChangedBy(source.getChangedBy());
        target.setLastModified(source.getLastModified());
    }
}
