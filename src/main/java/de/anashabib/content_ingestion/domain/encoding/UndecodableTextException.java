package de.anashabib.content_ingestion.domain.encoding;

public class UndecodableTextException extends RuntimeException {
    public UndecodableTextException(String message) {
        super(message);
    }
}