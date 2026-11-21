package de.anashabib.content_ingestion.domain.model;

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
) {}