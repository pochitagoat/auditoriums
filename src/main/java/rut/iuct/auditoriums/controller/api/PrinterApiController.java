package rut.iuct.auditoriums.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rut.iuct.auditoriums.dto.*;
import rut.iuct.auditoriums.model.EquipmentStatus;
import rut.iuct.auditoriums.service.PrinterService;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class PrinterApiController {
    private final PrinterService service;

    public PrinterApiController(PrinterService service) { this.service = service; }

    @GetMapping("/auditoriums/{auditoriumId}/printers")
    public List<PrinterResponse> list(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long auditoriumId,
            @RequestParam(defaultValue = "false") boolean includeWrittenOff) {
        return service.list(auditoriumId, includeWrittenOff);
    }

    @PostMapping("/auditoriums/{auditoriumId}/printers")
    public ResponseEntity<PrinterResponse> create(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long auditoriumId,
            @RequestBody @Validated(ValidationGroups.Create.class) PrinterRequest request) {
        PrinterResponse result = service.create(auditoriumId, request);
        return ResponseEntity.created(URI.create("/api/printers/" + result.getId())).body(result);
    }

    @GetMapping("/printers/{id}")
    public PrinterResponse get(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id) { return service.get(id); }

    @PutMapping("/printers/{id}")
    public PrinterResponse update(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestBody @Validated(ValidationGroups.Update.class) PrinterRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/printers/{id}/write-off")
    public PrinterResponse writeOff(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestBody @Valid StatusChangeRequest request) {
        return service.changeStatus(id, EquipmentStatus.WRITTEN_OFF, request);
    }

    @PatchMapping("/printers/{id}/restore")
    public PrinterResponse restore(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestBody @Valid StatusChangeRequest request) {
        return service.changeStatus(id, EquipmentStatus.ACTIVE, request);
    }

    @DeleteMapping("/printers/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestParam(required = false) String changedBy,
            @RequestParam @PositiveOrZero(message = "Версия не может быть отрицательной") Long version) {
        service.delete(id, changedBy, version);
        return ResponseEntity.noContent().build();
    }
}
