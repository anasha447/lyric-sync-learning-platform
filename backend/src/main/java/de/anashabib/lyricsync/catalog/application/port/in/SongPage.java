package de.anashabib.lyricsync.catalog.application.port.in;

import java.util.List;

/** One page of results. Our own type, so the application doesn't depend on Spring Data's Page. */
public record SongPage<T>(List<T> items, int page, int size, long totalItems) {
    public SongPage {
        items = List.copyOf(items);
    }

    public int totalPages() {
        return (int) Math.ceil((double) totalItems / size);
    }
}
