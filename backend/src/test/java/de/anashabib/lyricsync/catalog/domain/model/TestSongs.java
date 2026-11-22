package de.anashabib.lyricsync.catalog.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Valid songs for tests, so each test only spells out what it's about. */
public final class TestSongs {
    private TestSongs() {
    }

    public static Song draft() {
        return draft(UUID.randomUUID(), List.of(line(0, 15_260, 19_840, "Sie ist ein Model")));
    }

    public static Song draft(UUID artistId, List<SongLine> lines) {
        return Song.draft(
                Slug.fromTitle("Das Model"), "Das Model", artistId, CefrLevel.B1, 218_000,
                LicenseType.WRITTEN_PERMISSION, "Permission from the label, 2026-11-01",
                new AudioKey("audio/das-model.mp3"), lines);
    }

    /** The song as the repository hands it back: with an id, version 0 and a creation time. */
    public static Song saved(Song s) {
        return new Song(UUID.randomUUID(), s.slug(), s.title(), s.artistId(), s.languageCode(), s.cefrLevel(),
                s.durationMs(), s.status(), s.failureReason(), s.licenseType(), s.licenseNote(), s.audioKey(),
                s.coverImageUrl(), s.syncGranularity(), s.lines(), 0L, Instant.parse("2026-11-22T10:00:00Z"),
                s.publishedAt());
    }

    /** A song the library shows: analysed and published. */
    public static Song published(UUID artistId, String title, CefrLevel level) {
        return Song.draft(Slug.fromTitle(title), title, artistId, level, 60_000, LicenseType.PUBLIC_DOMAIN,
                        "Public domain, Wikisource", new AudioKey("audio/" + Slug.fromTitle(title).value() + ".mp3"),
                        List.of(line(0, 1_000, 5_000, "Erste Zeile"), line(1, 5_000, 9_000, "Zweite Zeile")))
                .submitForAnalysis().analysisSucceeded().publish(Instant.parse("2026-11-22T10:00:00Z"));
    }

    public static SongLine line(int index, int startMs, int endMs, String text) {
        return new SongLine(null, index, text, startMs, endMs, text.isBlank(), null, null);
    }
}
