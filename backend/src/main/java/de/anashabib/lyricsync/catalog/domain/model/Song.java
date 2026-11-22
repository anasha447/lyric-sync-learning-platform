package de.anashabib.lyricsync.catalog.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Song(
        UUID id,
        String title,
        UUID artistId,
        String languageCode,
        String cefrLevel,
        int durationMs,
        LicenseType licenseType,
        AudioKey audioKey,
        String coverImageUrl,
        SyncGranularity syncGranularity,
        List<SongLine> lines,
        Instant createdAt
) {}