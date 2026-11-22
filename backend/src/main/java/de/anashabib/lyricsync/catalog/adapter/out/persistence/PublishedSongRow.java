package de.anashabib.lyricsync.catalog.adapter.out.persistence;

import de.anashabib.lyricsync.catalog.domain.model.CefrLevel;

/** Filled straight from the query with a JPQL constructor expression, no entities loaded. */
public record PublishedSongRow(String slug, String title, String artistName, CefrLevel level, int durationMs) {
}
