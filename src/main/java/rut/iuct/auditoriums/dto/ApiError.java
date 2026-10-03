package rut.iuct.auditoriums.dto;

import jakarta.validation.constraints.*;
import rut.iuct.auditoriums.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class ApiError {

    private LocalDateTime timestamp;

    private int status;

    private String error;

    private String message;

    private String path;

    private java.util.Map<String, String> fieldErrors;

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public java.util.Map<String, String> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(java.util.Map<String, String> fieldErrors) { this.fieldErrors = fieldErrors; }

}
