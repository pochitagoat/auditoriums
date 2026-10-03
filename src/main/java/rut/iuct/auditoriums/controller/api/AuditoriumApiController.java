package rut.iuct.auditoriums.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rut.iuct.auditoriums.dto.*;
import rut.iuct.auditoriums.exception.ApiException;
import rut.iuct.auditoriums.service.AuditoriumService;
import rut.iuct.auditoriums.service.AuditoriumSearchService;
import java.net.URI;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/auditoriums", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuditoriumApiController {
    private static final Set<String> FILTER_FIELDS = Set.of("q", "type", "building", "floor", "hasProjector",
            "hasPrinter", "hasWorkstations", "internet", "hasAc", "boardType", "departmentId", "page", "size");
    private final AuditoriumService service;
    private final AuditoriumSearchService searchService;

    public AuditoriumApiController(AuditoriumService service, AuditoriumSearchService searchService) {
        this.service = service;
        this.searchService = searchService;
    }

    @GetMapping
    public PageResponse<AuditoriumListItem> list(@Valid @ModelAttribute AuditoriumFilter filter, HttpServletRequest request) {
        for (String parameter : request.getParameterMap().keySet()) {
            if (!FILTER_FIELDS.contains(parameter)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Неизвестный параметр поиска", Map.of(parameter, "Этот фильтр не поддерживается"));
            }
        }
        return searchService.search(filter);
    }

    @GetMapping("/{id}")
    public AuditoriumResponse get(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id) { return service.get(id); }

    @PostMapping
    public ResponseEntity<AuditoriumResponse> create(@RequestBody @Validated(ValidationGroups.Create.class) AuditoriumRequest request) {
        AuditoriumResponse result = service.create(request);
        return ResponseEntity.created(URI.create("/api/auditoriums/" + result.getId())).body(result);
    }

    @PutMapping("/{id}")
    public AuditoriumResponse update(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestBody @Validated(ValidationGroups.Update.class) AuditoriumRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive(message = "Идентификатор должен быть положительным") Long id,
            @RequestParam String changedBy,
            @RequestParam @PositiveOrZero(message = "Версия не может быть отрицательной") Long version) {
        service.delete(id, changedBy, version);
        return ResponseEntity.noContent().build();
    }
}
