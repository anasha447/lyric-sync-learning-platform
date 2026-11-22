package de.anashabib.lyricsync.catalog.application.port.in;

import de.anashabib.lyricsync.catalog.domain.model.CefrLevel;
import de.anashabib.lyricsync.catalog.domain.model.Slug;

import java.util.Optional;

/** What learners (and guests) can read: published songs only. */
public interface BrowseSongsUseCase {
    int PAGE_SIZE = 20;

    /** @param level null for every level */
    SongPage<SongSummary> listPublished(CefrLevel level, int page);

    Optional<PublishedSong> findPublished(Slug slug);
}
