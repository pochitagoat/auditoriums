package rut.iuct.auditoriums.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rut.iuct.auditoriums.dto.*;
import rut.iuct.auditoriums.service.AdditionalEquipmentLinkService;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/api/auditoriums/{auditoriumId}/additional-equipment", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdditionalEquipmentLinkApiController {
    private final AdditionalEquipmentLinkService service;

    public AdditionalEquipmentLinkApiController(AdditionalEquipmentLinkService service) { this.service = service; }

    @GetMapping
    public List<AdditionalEquipmentResponse> list(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long auditoriumId) {
        return service.list(auditoriumId);
    }

    @PostMapping
    public ResponseEntity<AdditionalEquipmentResponse> add(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long auditoriumId,
            @RequestBody @Valid EquipmentLinkRequest request) {
        AdditionalEquipmentResponse result = service.add(auditoriumId, request);
        return ResponseEntity.created(URI.create("/api/auditoriums/" + auditoriumId + "/additional-equipment/" + result.getId())).body(result);
    }

    @DeleteMapping("/{additionalEquipmentId}")
    public ResponseEntity<Void> remove(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long auditoriumId,
            @PathVariable @Positive(message = "Идентификатор должен быть положительным") Long additionalEquipmentId,
            @RequestParam String changedBy,
            @RequestParam @PositiveOrZero(message = "Версия не может быть отрицательной") Long version) {
        service.remove(auditoriumId, additionalEquipmentId, changedBy, version);
        return ResponseEntity.noContent().build();
    }
}
