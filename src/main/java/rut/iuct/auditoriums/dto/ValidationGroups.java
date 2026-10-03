package rut.iuct.auditoriums.dto;

import jakarta.validation.groups.Default;

public final class ValidationGroups {
    private ValidationGroups() {
    }

    public interface Create extends Default {}
    public interface Update extends Default {}
}
