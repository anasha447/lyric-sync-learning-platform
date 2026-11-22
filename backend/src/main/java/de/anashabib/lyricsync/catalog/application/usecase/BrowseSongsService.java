package de.anashabib.lyricsync.catalog.application.usecase;

import de.anashabib.lyricsync.catalog.application.port.in.BrowseSongsUseCase;
import de.anashabib.lyricsync.catalog.application.port.in.PublishedSong;
import de.anashabib.lyricsync.catalog.application.port.in.SongPage;
import de.anashabib.lyricsync.catalog.application.port.in.SongSummary;
import de.anashabib.lyricsync.catalog.application.port.out.PublishedSongQueryPort;
import de.anashabib.lyricsync.catalog.domain.model.CefrLevel;
import de.anashabib.lyricsync.catalog.domain.model.Slug;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Published songs change rarely and are read by every learner, so they're kept in memory.
 * {@link PublishedSongCacheEvictor} empties the caches whenever a song is published, unpublished
 * or re-analysed; the time limit in application.yaml is only a safety net.
 */
@Component
@RequiredArgsConstructor
public class BrowseSongsService implements BrowseSongsUseCase {

    private final PublishedSongQueryPort query;

    @Override
    @Cacheable(cacheNames = CatalogCaches.PUBLISHED_SONG_PAGES, key = "#level + ':' + #page")
    public SongPage<SongSummary> listPublished(CefrLevel level, int page) {
        return query.findPublished(level, page, PAGE_SIZE);
    }

    // "not found" isn't cached: random slugs would otherwise fill the cache
    @Override
    @Cacheable(cacheNames = CatalogCaches.PUBLISHED_SONGS, key = "#slug.value()", unless = "#result == null")
    public Optional<PublishedSong> findPublished(Slug slug) {
        return query.findPublishedBySlug(slug);
    }
}
