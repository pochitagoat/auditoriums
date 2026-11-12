package rut.iuct.auditoriums.controller.api;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rut.iuct.auditoriums.dto.*;
import rut.iuct.auditoriums.service.AdditionalEquipmentService;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/api/additional-equipment", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdditionalEquipmentApiController {
    private final AdditionalEquipmentService service;

    public AdditionalEquipmentApiController(AdditionalEquipmentService service) { this.service = service; }

    @GetMapping
    public List<AdditionalEquipmentResponse> list() { return service.list(); }

    @GetMapping("/{id}")
    public AdditionalEquipmentResponse get(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id) {
        return service.get(id);
    }

    @PostMapping
    public ResponseEntity<AdditionalEquipmentResponse> create(@RequestBody @Validated(ValidationGroups.Create.class) AdditionalEquipmentRequest request) {
        AdditionalEquipmentResponse result = service.create(request);
        return ResponseEntity.created(URI.create("/api/additional-equipment/" + result.getId())).body(result);
    }

    @PutMapping("/{id}")
    public AdditionalEquipmentResponse update(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestBody @Validated(ValidationGroups.Update.class) AdditionalEquipmentRequest request) {
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
