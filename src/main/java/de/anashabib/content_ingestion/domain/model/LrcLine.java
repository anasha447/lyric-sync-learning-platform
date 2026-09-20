package de.anashabib.content_ingestion.domain.model;

public record LrcLine(int startMs, Integer endMs, String text, boolean instrumental) {
    public LrcLine(int startMs, String text) {
        this(startMs, null, text, text == null || text.isBlank());
    }
}