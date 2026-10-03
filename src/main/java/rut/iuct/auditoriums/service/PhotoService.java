package rut.iuct.auditoriums.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rut.iuct.auditoriums.dto.*;
import rut.iuct.auditoriums.exception.ApiException;
import rut.iuct.auditoriums.mapper.PhotoMapper;
import rut.iuct.auditoriums.model.*;
import rut.iuct.auditoriums.repository.*;
import rut.iuct.auditoriums.storage.*;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PhotoService {
    private final AuditoriumPhotoRepository photos;
    private final AuditoriumRepository auditoriums;
    private final PhotoMapper mapper;
    private final AuditSupport audit;
    private final PhotoValidator validator;
    private final FileStorageService storage;
    private final FileCleanupService cleanup;

    public PhotoService(AuditoriumPhotoRepository photos, AuditoriumRepository auditoriums, PhotoMapper mapper,
            AuditSupport audit, PhotoValidator validator, FileStorageService storage, FileCleanupService cleanup) {
        this.photos = photos;
        this.auditoriums = auditoriums;
        this.mapper = mapper;
        this.audit = audit;
        this.validator = validator;
        this.storage = storage;
        this.cleanup = cleanup;
    }

    private Auditorium auditorium(Long id) {
        return auditoriums.findById(id).orElseThrow(() -> ApiException.notFound("Аудитория не найдена"));
    }

    private AuditoriumPhoto find(Long id) {
        return photos.findById(id).orElseThrow(() -> ApiException.notFound("Фотография не найдена"));
    }

    public List<PhotoResponse> list(Long auditoriumId) {
        auditorium(auditoriumId);
        return photos.findByAuditorium_IdOrderByIdAsc(auditoriumId).stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public PhotoResponse upload(Long auditoriumId, MultipartFile file, String changedBy, Long auditoriumVersion) {
        EquipmentRequest operation = new EquipmentRequest();
        operation.setChangedBy(changedBy);
        operation.setAuditoriumVersion(auditoriumVersion);
        audit.validate(operation, ValidationGroups.Create.class);
        Auditorium parent = auditorium(auditoriumId);
        audit.checkVersion(parent, auditoriumVersion);
        byte[] content = validator.validate(file);
        String contentType = PhotoValidator.contentType(content);
        String key = storage.newKey(contentType);
        cleanup.trackUpload(key);
        storage.write(key, content);
        AuditoriumPhoto photo = new AuditoriumPhoto(null, key, file.getOriginalFilename(), contentType, parent);
        audit.touch(photo, changedBy);
        audit.touch(parent, changedBy);
        photos.saveAndFlush(photo);
        cleanup.completeUpload(key);
        return mapper.toResponse(photo);
    }

    public byte[] content(Long id) {
        AuditoriumPhoto photo = find(id);
        try { return storage.read(photo.getFilePath()); }
        catch (StorageException exception) {
            org.slf4j.LoggerFactory.getLogger(PhotoService.class).error("Файл фотографии {} недоступен", id, exception);
            throw new ApiException(HttpStatus.NOT_FOUND, "Файл фотографии недоступен. Сообщите ответственному сотруднику", Map.of());
        }
    }

    @Transactional
    public void delete(Long id, String changedBy, Long version) {
        audit.validateDeletion(changedBy, version);
        AuditoriumPhoto photo = find(id);
        audit.checkVersion(photo, version);
        audit.touch(photo.getAuditorium(), changedBy);
        cleanup.deleteAfterCommit(photo.getFilePath());
        photos.delete(photo);
        photos.flush();
    }

    // Вызывается в общей транзакции удаления пустой аудитории.
    @Transactional
    public void deleteForAuditorium(Long id) {
        for (AuditoriumPhoto photo : photos.findByAuditorium_IdOrderByIdAsc(id)) {
            cleanup.deleteAfterCommit(photo.getFilePath());
            photos.delete(photo);
        }
        photos.flush();
    }
}
