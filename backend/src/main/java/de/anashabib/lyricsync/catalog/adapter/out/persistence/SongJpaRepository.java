package de.anashabib.lyricsync.catalog.adapter.out.persistence;

import de.anashabib.lyricsync.catalog.adapter.out.persistence.entity.SongEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SongJpaRepository extends JpaRepository<SongEntity, UUID> {
    boolean existsBySlug(String slug);
}
