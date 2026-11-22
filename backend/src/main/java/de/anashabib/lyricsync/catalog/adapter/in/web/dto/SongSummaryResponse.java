package de.anashabib.lyricsync.catalog.adapter.in.web.dto;

import de.anashabib.lyricsync.catalog.application.port.in.SongSummary;

public record SongSummaryResponse(String slug, String title, String artist, String level, int durationMs) {
    public static SongSummaryResponse from(SongSummary s) {
        return new SongSummaryResponse(s.slug(), s.title(), s.artistName(),
                s.level() == null ? null : s.level().name(), s.durationMs());
    }
}
