package de.anashabib.content_ingestion.adapter.out.persistence;

import de.anashabib.content_ingestion.adapter.out.persistence.mapper.ArtistMapper;
import de.anashabib.content_ingestion.application.port.out.ArtistRepositoryPort;
import de.anashabib.content_ingestion.domain.model.Artist;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ArtistPersistenceAdapter implements ArtistRepositoryPort {

    private final ArtistJpaRepository jpaRepository;
    private final ArtistMapper mapper;

    @Override
    @Transactional
    public Artist save(Artist artist) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(artist)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Artist> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Artist> findByName(String name) {
        return jpaRepository.findByName(name).map(mapper::toDomain);
    }
}