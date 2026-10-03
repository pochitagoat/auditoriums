package rut.iuct.auditoriums.mapper;

import org.springframework.stereotype.Component;
import rut.iuct.auditoriums.model.Printer;
import rut.iuct.auditoriums.dto.PrinterRequest;
import rut.iuct.auditoriums.dto.PrinterResponse;

@Component
public class PrinterMapper {
    public PrinterResponse toResponse(Printer source) {
        PrinterResponse target = new PrinterResponse();
        AuditMapper.copy(source, source.getId(), target);
        target.setAuditoriumId(source.getAuditorium().getId());
        target.setAuditoriumVersion(source.getAuditorium().getVersion());
        target.setStatus(source.getStatus());
        target.setWrittenOffAt(source.getWrittenOffAt());
        target.setWrittenOffBy(source.getWrittenOffBy());
        target.setModel(source.getModel());
        target.setWarehouse(source.getWarehouse());
        target.setCartridgeLink(source.getCartridgeLink());
        return target;
    }

    public void apply(PrinterRequest source, Printer target) {
        target.setModel(source.getModel());
        target.setWarehouse(source.getWarehouse());
        target.setCartridgeLink(source.getCartridgeLink());
    }
}
