package rut.iuct.auditoriums.dto;

public class PhotoResponse extends AuditedResponse {
    private Long auditoriumId;
    private String originalFilename;
    private String contentType;
    private String url;

    public Long getAuditoriumId() { return auditoriumId; }
    public void setAuditoriumId(Long auditoriumId) { this.auditoriumId = auditoriumId; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
