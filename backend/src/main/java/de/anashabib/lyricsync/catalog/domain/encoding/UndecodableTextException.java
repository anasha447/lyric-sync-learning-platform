package de.anashabib.lyricsync.catalog.domain.encoding;

public class UndecodableTextException extends RuntimeException {
    public UndecodableTextException(String message) {
        super(message);
    }
}