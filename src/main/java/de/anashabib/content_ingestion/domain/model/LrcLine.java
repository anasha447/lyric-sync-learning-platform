package de.anashabib.content_ingestion.domain.model;

public record LrcLine(int startMs, Integer endMs, String text, boolean instrumental) {
    public LrcLine(int startMs, String text) {
        this(startMs, null, text, text == null || text.isBlank());
    }
    public LrcLine shiftedBy(int offsetMs) {
        return new LrcLine(
                Math.max(0, startMs + offsetMs), // clamp to 0 so time is never negative
                endMs == null ? null : Math.max(0, endMs + offsetMs),
                text,
                instrumental
        );
    }
    public LrcLine withEndMs(int newEndMs) {
        return new LrcLine(startMs, newEndMs, text, instrumental);
    }
}