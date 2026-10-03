package rut.iuct.auditoriums.mapper;

import org.springframework.stereotype.Component;
import rut.iuct.auditoriums.dto.PhotoResponse;
import rut.iuct.auditoriums.model.AuditoriumPhoto;

@Component
public class PhotoMapper {
    public PhotoResponse toResponse(AuditoriumPhoto entity) {
        PhotoResponse result = new PhotoResponse();
        result.setId(entity.getId());
        result.setVersion(entity.getVersion());
        result.setChangedBy(entity.getChangedBy());
        result.setLastModified(entity.getLastModified());
        result.setAuditoriumId(entity.getAuditorium().getId());
        result.setOriginalFilename(entity.getOriginalFilename());
        result.setContentType(entity.getContentType());
        result.setUrl("/api/photos/" + entity.getId() + "/content");
        return result;
    }
}
