package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class DepartmentResponse extends AuditedResponse {

    private String name;

    private String molLink;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMolLink() { return molLink; }
    public void setMolLink(String molLink) { this.molLink = molLink; }

}
