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
import rut.iuct.auditoriums.service.WorkstationService;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class WorkstationApiController {
    private final WorkstationService service;

    public WorkstationApiController(WorkstationService service) { this.service = service; }

    @GetMapping("/auditoriums/{auditoriumId}/workstations")
    public List<WorkstationResponse> list(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long auditoriumId,
            @RequestParam(defaultValue = "false") boolean includeWrittenOff) {
        return service.list(auditoriumId, includeWrittenOff);
    }

    @PostMapping("/auditoriums/{auditoriumId}/workstations")
    public ResponseEntity<WorkstationResponse> create(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long auditoriumId,
            @RequestBody @Validated(ValidationGroups.Create.class) WorkstationRequest request) {
        WorkstationResponse result = service.create(auditoriumId, request);
        return ResponseEntity.created(URI.create("/api/workstations/" + result.getId())).body(result);
    }

    @GetMapping("/workstations/{id}")
    public WorkstationResponse get(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id) { return service.get(id); }

    @PutMapping("/workstations/{id}")
    public WorkstationResponse update(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestBody @Validated(ValidationGroups.Update.class) WorkstationRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/workstations/{id}/write-off")
    public WorkstationResponse writeOff(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestBody @Valid StatusChangeRequest request) {
        return service.changeStatus(id, EquipmentStatus.WRITTEN_OFF, request);
    }

    @PatchMapping("/workstations/{id}/restore")
    public WorkstationResponse restore(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestBody @Valid StatusChangeRequest request) {
        return service.changeStatus(id, EquipmentStatus.ACTIVE, request);
    }

    @DeleteMapping("/workstations/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestParam(required = false) String changedBy,
            @RequestParam @PositiveOrZero(message = "Версия не может быть отрицательной") Long version) {
        service.delete(id, changedBy, version);
        return ResponseEntity.noContent().build();
    }
}
