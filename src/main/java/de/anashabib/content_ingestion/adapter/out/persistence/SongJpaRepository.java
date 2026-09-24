package de.anashabib.content_ingestion.adapter.out.persistence;

import de.anashabib.content_ingestion.adapter.out.persistence.entity.SongEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SongJpaRepository extends JpaRepository<SongEntity, UUID> {
}