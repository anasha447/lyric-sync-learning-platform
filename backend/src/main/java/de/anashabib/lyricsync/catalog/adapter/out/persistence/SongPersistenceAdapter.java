package de.anashabib.lyricsync.catalog.adapter.out.persistence;

import de.anashabib.lyricsync.catalog.adapter.out.persistence.mapper.SongMapper;
import de.anashabib.lyricsync.catalog.application.port.out.SongRepositoryPort;
import de.anashabib.lyricsync.catalog.domain.model.Song;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SongPersistenceAdapter implements SongRepositoryPort {

    private final SongJpaRepository jpaRepository;
    private final SongMapper mapper;

    @Override
    @Transactional
    public Song save(Song song) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(song)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Song> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}