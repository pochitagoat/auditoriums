package rut.iuct.auditoriums.controller.api;

import jakarta.validation.constraints.Positive;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rut.iuct.auditoriums.dto.PhotoResponse;
import rut.iuct.auditoriums.exception.ApiException;
import rut.iuct.auditoriums.service.PhotoService;
import rut.iuct.auditoriums.storage.PhotoValidator;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PhotoApiController {
    private final PhotoService service;
    public PhotoApiController(PhotoService service) { this.service = service; }

    @GetMapping(value = "/auditoriums/{id}/photos", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PhotoResponse> list(@PathVariable @Positive Long id) { return service.list(id); }

    @PostMapping(value = "/auditoriums/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PhotoResponse> upload(@PathVariable @Positive Long id, @RequestPart MultipartFile file,
            @RequestParam(required = false) String changedBy, @RequestParam Long auditoriumVersion) {
        PhotoResponse result = service.upload(id, file, changedBy, auditoriumVersion);
        return ResponseEntity.created(URI.create(result.getUrl())).body(result);
    }

    @GetMapping("/photos/{photoId}/content")
    public ResponseEntity<byte[]> content(@PathVariable @Positive Long photoId) {
        byte[] content = service.content(photoId);
        String type = PhotoValidator.contentType(content);
        if (type == null) throw ApiException.notFound("Файл фотографии имеет неподдерживаемый формат");
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(type))
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.noCache()).body(content);
    }

    @DeleteMapping("/photos/{photoId}")
    public ResponseEntity<Void> delete(@PathVariable @Positive Long photoId, @RequestParam(required = false) String changedBy, @RequestParam Long version) {
        service.delete(photoId, changedBy, version);
        return ResponseEntity.noContent().build();
    }
}
