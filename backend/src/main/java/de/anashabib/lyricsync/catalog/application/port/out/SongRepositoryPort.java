package de.anashabib.lyricsync.catalog.application.port.out;

import de.anashabib.lyricsync.catalog.domain.model.Slug;
import de.anashabib.lyricsync.catalog.domain.model.Song;

import java.util.Optional;
import java.util.UUID;

public interface SongRepositoryPort {
    Song save(Song song);
    Optional<Song> findById(UUID id);
    boolean existsBySlug(Slug slug);
}
