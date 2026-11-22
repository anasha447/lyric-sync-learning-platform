package de.anashabib.lyricsync.catalog.application.usecase;

/** Cache names, configured in application.yaml (Caffeine, ADR-0011). */
public final class CatalogCaches {
    public static final String PUBLISHED_SONGS = "publishedSongs";
    public static final String PUBLISHED_SONG_PAGES = "publishedSongPages";

    private CatalogCaches() {
    }
}
