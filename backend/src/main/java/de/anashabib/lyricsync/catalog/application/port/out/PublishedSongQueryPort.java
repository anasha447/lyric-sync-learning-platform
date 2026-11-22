package de.anashabib.lyricsync.catalog.application.port.out;

import de.anashabib.lyricsync.catalog.application.port.in.PublishedSong;
import de.anashabib.lyricsync.catalog.application.port.in.SongPage;
import de.anashabib.lyricsync.catalog.application.port.in.SongSummary;
import de.anashabib.lyricsync.catalog.domain.model.CefrLevel;
import de.anashabib.lyricsync.catalog.domain.model.Slug;

import java.util.Optional;

/**
 * Reads for the public side. Separate from SongRepositoryPort, which loads and saves whole songs:
 * the library list needs a join with the artist and no lines at all.
 */
public interface PublishedSongQueryPort {
    SongPage<SongSummary> findPublished(CefrLevel level, int page, int size);

    Optional<PublishedSong> findPublishedBySlug(Slug slug);
}
