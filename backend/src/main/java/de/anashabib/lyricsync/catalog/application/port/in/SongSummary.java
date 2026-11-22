package de.anashabib.lyricsync.catalog.application.port.in;

import de.anashabib.lyricsync.catalog.domain.model.CefrLevel;

/** One row of the library: enough for a song card, no lines. */
public record SongSummary(String slug, String title, String artistName, CefrLevel level, int durationMs) {
}
