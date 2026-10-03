package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class DepartmentRequest extends VersionedRequest {

    @NotBlank(message = "Укажите название")
    @Size(max = 255, message = "Допустимо не более 255 символов")
    private String name;

    @Size(max = 2048, message = "Допустимо не более 2048 символов")
    private String molLink;

    public String getName() { return name; }
    public void setName(String name) { this.name = TextValues.required(name); }

    public String getMolLink() { return molLink; }
    public void setMolLink(String molLink) { this.molLink = TextValues.optional(molLink); }

}
