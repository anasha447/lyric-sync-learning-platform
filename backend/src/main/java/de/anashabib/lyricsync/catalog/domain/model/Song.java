package de.anashabib.lyricsync.catalog.domain.model;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * A recording with its timed lines, artist, level and license. Immutable: a status change
 * returns a new Song. The constructor refuses any combination that can't exist, so code
 * holding a Song never has to re-check it.
 *
 * @param cefrLevel   optional until publishing
 * @param failureReason set exactly when the status is FAILED
 * @param version     null until saved; the database bumps it on every update (used as the ETag)
 * @param publishedAt set exactly when the status is PUBLISHED
 */
public record Song(
        UUID id,
        Slug slug,
        String title,
        UUID artistId,
        String languageCode,
        CefrLevel cefrLevel,
        int durationMs,
        SongStatus status,
        String failureReason,
        LicenseType licenseType,
        String licenseNote,
        AudioKey audioKey,
        String coverImageUrl,
        SyncGranularity syncGranularity,
        List<SongLine> lines,
        Long version,
        Instant createdAt,
        Instant publishedAt
) {
    public Song {
        require(!isBlank(title), "A song needs a title");
        require(slug != null, "A song needs a slug");
        require(artistId != null, "A song needs an artist");
        require(!isBlank(languageCode), "A song needs a language");
        require(durationMs > 0, "Duration must be positive, was " + durationMs + "ms");
        require(status != null, "A song needs a status");
        require(licenseType != null, "A song needs a license");
        require(!isBlank(licenseNote), "A song needs a license note: source, attribution or permission");
        require(audioKey != null, "A song needs audio");
        require(syncGranularity != null, "A song needs a sync granularity");
        require(lines != null && !lines.isEmpty(), "A song needs at least one line");
        lines = List.copyOf(lines);
        checkLines(lines, durationMs);

        require((status == SongStatus.FAILED) == !isBlank(failureReason),
                "A failure reason belongs to a FAILED song, and a FAILED song needs one");
        require((status == SongStatus.PUBLISHED) == (publishedAt != null),
                "A publish date belongs to a PUBLISHED song, and a PUBLISHED song needs one");
        require(status != SongStatus.PUBLISHED || cefrLevel != null, "A published song needs a level");
    }

    /** A new song, stored but not yet sent for analysis. */
    public static Song draft(Slug slug, String title, UUID artistId, CefrLevel cefrLevel, int durationMs,
                             LicenseType licenseType, String licenseNote, AudioKey audioKey,
                             List<SongLine> lines) {
        return new Song(null, slug, title, artistId, "de", cefrLevel, durationMs, SongStatus.DRAFT, null,
                licenseType, licenseNote, audioKey, null, SyncGranularity.LINE, lines, null, null, null);
    }

    public Song submitForAnalysis() {
        requireStatus(SongStatus.DRAFT, SongStatus.FAILED);
        return withStatus(SongStatus.PROCESSING, null, null);
    }

    public Song analysisSucceeded() {
        requireStatus(SongStatus.PROCESSING);
        return withStatus(SongStatus.READY_FOR_REVIEW, null, null);
    }

    public Song analysisFailed(String reason) {
        requireStatus(SongStatus.PROCESSING);
        return withStatus(SongStatus.FAILED, reason, null);
    }

    /** Sprint 6 adds the second rule from the spec: no unmatched tokens. */
    public Song publish(Instant now) {
        requireStatus(SongStatus.READY_FOR_REVIEW);
        if (cefrLevel == null) {
            throw new IllegalStateException("Set a level before publishing");
        }
        return withStatus(SongStatus.PUBLISHED, null, now);
    }

    public Song unpublish() {
        requireStatus(SongStatus.PUBLISHED);
        return withStatus(SongStatus.READY_FOR_REVIEW, null, null);
    }

    private Song withStatus(SongStatus newStatus, String newFailureReason, Instant newPublishedAt) {
        return new Song(id, slug, title, artistId, languageCode, cefrLevel, durationMs, newStatus,
                newFailureReason, licenseType, licenseNote, audioKey, coverImageUrl, syncGranularity,
                lines, version, createdAt, newPublishedAt);
    }

    private void requireStatus(SongStatus... allowed) {
        if (!Arrays.asList(allowed).contains(status)) {
            throw new IllegalStateException(
                    "Not allowed while the song is " + status + " (needs " + Arrays.toString(allowed) + ")");
        }
    }

    private static void checkLines(List<SongLine> lines, int durationMs) {
        for (int i = 0; i < lines.size(); i++) {
            SongLine line = lines.get(i);
            require(line.lineIndex() == i, "Line index " + line.lineIndex() + " at position " + i);
            require(line.endMs() <= durationMs, "Line " + i + " ends after the track ends");
            if (i > 0) {
                require(line.startMs() > lines.get(i - 1).startMs(),
                        "Line " + i + " must start after line " + (i - 1));
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
