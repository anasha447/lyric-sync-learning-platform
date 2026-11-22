package de.anashabib.lyricsync.catalog.adapter.in.web.dto;

import de.anashabib.lyricsync.catalog.domain.model.Song;
import de.anashabib.lyricsync.catalog.domain.model.SongLine;

import java.util.List;
import java.util.UUID;

/**
 * What the admin API returns for a song. A separate shape from the domain record, so the
 * domain can change without breaking clients, and values read the way requests write them
 * ({@code "written_permission"}, not {@code "WRITTEN_PERMISSION"}).
 */
public record SongResponse(
        UUID id,
        String slug,
        String title,
        UUID artistId,
        String language,
        String level,
        int durationMs,
        String status,
        String licenseType,
        String licenseNote,
        String audioKey,
        Long version,
        List<Line> lines
) {
    public record Line(int index, String text, int startMs, int endMs, boolean instrumental) {
        static Line from(SongLine line) {
            return new Line(line.lineIndex(), line.rawText(), line.startMs(), line.endMs(), line.instrumental());
        }
    }

    public static SongResponse from(Song song) {
        return new SongResponse(
                song.id(), song.slug().value(), song.title(), song.artistId(), song.languageCode(),
                song.cefrLevel() == null ? null : song.cefrLevel().name(), song.durationMs(),
                song.status().name(), song.licenseType().dbValue(), song.licenseNote(), song.audioKey().value(),
                song.version(), song.lines().stream().map(Line::from).toList());
    }
}
