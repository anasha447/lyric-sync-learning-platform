package de.anashabib.content_ingestion.application.port.out;

import de.anashabib.content_ingestion.domain.model.Song;

import java.util.Optional;
import java.util.UUID;

public interface SongRepositoryPort {
    Song save(Song song);
    Optional<Song> findById(UUID id);
}