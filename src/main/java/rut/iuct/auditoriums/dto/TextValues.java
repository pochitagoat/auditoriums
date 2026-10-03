package rut.iuct.auditoriums.dto;

public final class TextValues {
    private TextValues() {
    }

    public static String required(String value) {
        return value == null ? null : value.strip();
    }

    public static String optional(String value) {
        if (value == null) return null;
        String normalized = value.strip();
        return normalized.isEmpty() ? null : normalized;
    }
}
