package de.anashabib.content_ingestion.domain.validation;

public record ValidationError(Integer lineIndex, String message) {
    // lineIndex == null → file-level error (nothing to highlight, e.g. "no lines at all")
    // lineIndex != null → point the frontend at that specific line
    public static ValidationError forLine(int lineIndex, String message) {
        return new ValidationError(lineIndex, message);
    }

    public static ValidationError fileLevel(String message) {
        return new ValidationError(null, message);
    }
}