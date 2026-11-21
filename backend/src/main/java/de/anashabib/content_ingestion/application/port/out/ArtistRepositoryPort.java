package de.anashabib.content_ingestion.application.port.out;

import de.anashabib.content_ingestion.domain.model.Artist;

import java.util.Optional;
import java.util.UUID;

public interface ArtistRepositoryPort {
    Artist save(Artist artist);
    Optional<Artist> findById(UUID id);
    Optional<Artist> findByName(String name);
}