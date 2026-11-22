package de.anashabib.lyricsync.catalog.application.usecase;

import de.anashabib.lyricsync.catalog.SongPublished;
import de.anashabib.lyricsync.catalog.SongReanalysed;
import de.anashabib.lyricsync.catalog.SongUnpublished;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Empties both caches when the library changes. Runs after the commit: evicting earlier would let
 * a request in between read the old row and put it straight back into the cache.
 * Whole caches, not single entries: a publish changes the song and every list page it appears on.
 */
@Component
public class PublishedSongCacheEvictor {

    @TransactionalEventListener(fallbackExecution = true)
    @CacheEvict(cacheNames = {CatalogCaches.PUBLISHED_SONGS, CatalogCaches.PUBLISHED_SONG_PAGES}, allEntries = true)
    public void on(SongPublished event) {
    }

    @TransactionalEventListener(fallbackExecution = true)
    @CacheEvict(cacheNames = {CatalogCaches.PUBLISHED_SONGS, CatalogCaches.PUBLISHED_SONG_PAGES}, allEntries = true)
    public void on(SongUnpublished event) {
    }

    @TransactionalEventListener(fallbackExecution = true)
    @CacheEvict(cacheNames = {CatalogCaches.PUBLISHED_SONGS, CatalogCaches.PUBLISHED_SONG_PAGES}, allEntries = true)
    public void on(SongReanalysed event) {
    }
}
