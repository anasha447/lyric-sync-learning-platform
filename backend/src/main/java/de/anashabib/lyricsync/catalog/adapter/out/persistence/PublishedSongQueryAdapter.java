package de.anashabib.lyricsync.catalog.adapter.out.persistence;

import de.anashabib.lyricsync.catalog.adapter.out.persistence.mapper.SongMapper;
import de.anashabib.lyricsync.catalog.application.port.in.PublishedSong;
import de.anashabib.lyricsync.catalog.application.port.in.SongPage;
import de.anashabib.lyricsync.catalog.application.port.in.SongSummary;
import de.anashabib.lyricsync.catalog.application.port.out.PublishedSongQueryPort;
import de.anashabib.lyricsync.catalog.domain.model.CefrLevel;
import de.anashabib.lyricsync.catalog.domain.model.Slug;
import de.anashabib.lyricsync.catalog.domain.model.SongStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PublishedSongQueryAdapter implements PublishedSongQueryPort {

    private final SongJpaRepository songs;
    private final ArtistJpaRepository artists;
    private final SongMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public SongPage<SongSummary> findPublished(CefrLevel level, int page, int size) {
        Page<PublishedSongRow> rows = songs.findPublished(SongStatus.PUBLISHED, level, PageRequest.of(page, size));
        return new SongPage<>(
                rows.map(r -> new SongSummary(r.slug(), r.title(), r.artistName(), r.level(), r.durationMs()))
                        .getContent(),
                page, size, rows.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PublishedSong> findPublishedBySlug(Slug slug) {
        return songs.findBySlugAndStatus(slug.value(), SongStatus.PUBLISHED)
                .map(entity -> new PublishedSong(
                        mapper.toDomain(entity),
                        artists.findById(entity.getArtistId()).orElseThrow().getName()));
    }
}
