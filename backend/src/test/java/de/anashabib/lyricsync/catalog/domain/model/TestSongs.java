package de.anashabib.lyricsync.catalog.domain.model;

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

    public static SongLine line(int index, int startMs, int endMs, String text) {
        return new SongLine(null, index, text, startMs, endMs, text.isBlank(), null, null);
    }
}
