package de.anashabib.lyricsync.catalog.domain.model;

import java.util.UUID;

public record SongLine(
        UUID id,
        int lineIndex,
        String rawText,
        int startMs,
        int endMs,
        boolean instrumental,
        String contextualTranslation,
        String culturalNote
) {
    public SongLine {
        if (lineIndex < 0) {
            throw new IllegalArgumentException("Line index can't be negative: " + lineIndex);
        }
        if (rawText == null) {
            throw new IllegalArgumentException("Line " + lineIndex + " has no text (use \"\" for instrumental)");
        }
        if (startMs < 0) {
            throw new IllegalArgumentException("Line " + lineIndex + " starts before the track: " + startMs + "ms");
        }
        if (endMs <= startMs) {
            throw new IllegalArgumentException(
                    "Line " + lineIndex + " ends at " + endMs + "ms, not after its start " + startMs + "ms");
        }
    }
}
