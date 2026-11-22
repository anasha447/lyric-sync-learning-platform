package de.anashabib.lyricsync.catalog.adapter.out.persistence;

import de.anashabib.lyricsync.catalog.adapter.out.persistence.entity.ArtistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface ArtistJpaRepository extends JpaRepository<ArtistEntity, UUID> {

    // lower() on both sides matches the unique index on lower(name), so Postgres can use it
    @Query("select a from ArtistEntity a where lower(a.name) = lower(:name)")
    Optional<ArtistEntity> findByNameIgnoringCase(@Param("name") String name);
}
