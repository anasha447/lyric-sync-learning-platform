package de.anashabib.lyricsync.catalog.application.port.out;

import de.anashabib.lyricsync.catalog.domain.model.Artist;

import java.util.Optional;
import java.util.UUID;

public interface ArtistRepositoryPort {
    Artist save(Artist artist);
    Optional<Artist> findById(UUID id);
    Optional<Artist> findByName(String name);
}