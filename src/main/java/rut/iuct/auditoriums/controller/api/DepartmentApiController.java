package rut.iuct.auditoriums.controller.api;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rut.iuct.auditoriums.dto.*;
import rut.iuct.auditoriums.service.DepartmentService;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/api/departments", produces = MediaType.APPLICATION_JSON_VALUE)
public class DepartmentApiController {
    private final DepartmentService service;

    public DepartmentApiController(DepartmentService service) { this.service = service; }

    @GetMapping
    public List<DepartmentResponse> list() { return service.list(); }

    @GetMapping("/{id}")
    public DepartmentResponse get(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@RequestBody @Validated(ValidationGroups.Create.class) DepartmentRequest request) {
        DepartmentResponse result = service.create(request);
        return ResponseEntity.created(URI.create("/api/departments/" + result.getId())).body(result);
    }

    @PutMapping("/{id}")
    public DepartmentResponse update(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestBody @Validated(ValidationGroups.Update.class) DepartmentRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestParam(required = false) String changedBy,
            @RequestParam @PositiveOrZero(message = "Версия не может быть отрицательной") Long version) {
        service.delete(id, changedBy, version);
        return ResponseEntity.noContent().build();
    }
}
