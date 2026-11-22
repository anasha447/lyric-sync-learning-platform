package de.anashabib.lyricsync.catalog.adapter.out.persistence;

import de.anashabib.lyricsync.catalog.adapter.out.persistence.entity.SongEntity;
import de.anashabib.lyricsync.catalog.domain.model.CefrLevel;
import de.anashabib.lyricsync.catalog.domain.model.SongStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface SongJpaRepository extends JpaRepository<SongEntity, UUID> {
    boolean existsBySlug(String slug);

    Optional<SongEntity> findBySlugAndStatus(String slug, SongStatus status);

    // a join with the artist for the name, and no lines: the library list never needs them
    @Query(value = """
            select new de.anashabib.lyricsync.catalog.adapter.out.persistence.PublishedSongRow(s.slug, s.title, a.name, s.cefrLevel, s.durationMs)
            from SongEntity s join ArtistEntity a on a.id = s.artistId
            where s.status = :status and (:level is null or s.cefrLevel = :level)
            order by s.title, s.slug
            """,
            countQuery = """
            select count(s) from SongEntity s
            where s.status = :status and (:level is null or s.cefrLevel = :level)
            """)
    Page<PublishedSongRow> findPublished(@Param("status") SongStatus status, @Param("level") CefrLevel level,
                                         Pageable pageable);
}
