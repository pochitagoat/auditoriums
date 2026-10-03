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
import rut.iuct.auditoriums.service.ProjectorService;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProjectorApiController {
    private final ProjectorService service;

    public ProjectorApiController(ProjectorService service) { this.service = service; }

    @GetMapping("/auditoriums/{auditoriumId}/projectors")
    public List<ProjectorResponse> list(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long auditoriumId,
            @RequestParam(defaultValue = "false") boolean includeWrittenOff) {
        return service.list(auditoriumId, includeWrittenOff);
    }

    @PostMapping("/auditoriums/{auditoriumId}/projectors")
    public ResponseEntity<ProjectorResponse> create(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long auditoriumId,
            @RequestBody @Validated(ValidationGroups.Create.class) ProjectorRequest request) {
        ProjectorResponse result = service.create(auditoriumId, request);
        return ResponseEntity.created(URI.create("/api/projectors/" + result.getId())).body(result);
    }

    @GetMapping("/projectors/{id}")
    public ProjectorResponse get(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id) { return service.get(id); }

    @PutMapping("/projectors/{id}")
    public ProjectorResponse update(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestBody @Validated(ValidationGroups.Update.class) ProjectorRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/projectors/{id}/write-off")
    public ProjectorResponse writeOff(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestBody @Valid StatusChangeRequest request) {
        return service.changeStatus(id, EquipmentStatus.WRITTEN_OFF, request);
    }

    @PatchMapping("/projectors/{id}/restore")
    public ProjectorResponse restore(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestBody @Valid StatusChangeRequest request) {
        return service.changeStatus(id, EquipmentStatus.ACTIVE, request);
    }

    @DeleteMapping("/projectors/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestParam String changedBy,
            @RequestParam @PositiveOrZero(message = "Версия не может быть отрицательной") Long version) {
        service.delete(id, changedBy, version);
        return ResponseEntity.noContent().build();
    }
}
