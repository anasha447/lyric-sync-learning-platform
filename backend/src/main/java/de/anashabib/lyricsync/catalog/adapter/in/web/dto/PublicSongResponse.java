package de.anashabib.lyricsync.catalog.adapter.in.web.dto;

import de.anashabib.lyricsync.catalog.application.port.in.PublishedSong;
import de.anashabib.lyricsync.catalog.domain.model.Song;
import de.anashabib.lyricsync.catalog.domain.model.SongLine;

import java.util.List;

/** What the player needs: lines with times, and where to fetch the audio. No admin fields. */
public record PublicSongResponse(String slug, String title, String artist, String level, int durationMs,
                                 String audioUrl, List<Line> lines) {

    public record Line(int index, String text, int startMs, int endMs, boolean instrumental, String translation) {
        static Line from(SongLine l) {
            return new Line(l.lineIndex(), l.rawText(), l.startMs(), l.endMs(), l.instrumental(),
                    l.contextualTranslation());
        }
    }

    public static PublicSongResponse from(PublishedSong published, String audioBaseUrl) {
        Song song = published.song();
        return new PublicSongResponse(song.slug().value(), song.title(), published.artistName(),
                song.cefrLevel().name(), song.durationMs(), audioBaseUrl + "/" + song.audioKey().value(),
                song.lines().stream().map(Line::from).toList());
    }
}
