package de.anashabib.lyricsync.catalog.adapter.out.persistence.mapper;

import de.anashabib.lyricsync.catalog.adapter.out.persistence.entity.SongEntity;
import de.anashabib.lyricsync.catalog.domain.model.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static de.anashabib.lyricsync.catalog.domain.model.TestSongs.line;
import static org.assertj.core.api.Assertions.assertThat;

class SongMapperTest {

    private final SongMapper mapper = new SongMapper();

    @Test
    void roundTripsASongWithItsLinesPreservingFieldsAndOrder() {
        Song song = TestSongs.draft(UUID.randomUUID(), List.of(
                line(0, 15_260, 19_840, "Sie ist ein Model"),
                line(1, 19_840, 24_100, "und sie sieht gut aus")));

        Song result = mapper.toDomain(mapper.toEntity(song));

        assertThat(result).usingRecursiveComparison().isEqualTo(song);
    }

    @Test
    void roundTripsLifecycleFields() {
        Song published = TestSongs.draft().submitForAnalysis().analysisSucceeded()
                .publish(Instant.parse("2026-11-22T10:00:00Z"));

        Song result = mapper.toDomain(mapper.toEntity(published));

        assertThat(result.status()).isEqualTo(SongStatus.PUBLISHED);
        assertThat(result.publishedAt()).isEqualTo(published.publishedAt());
        assertThat(result.slug()).isEqualTo(published.slug());
        assertThat(result.licenseNote()).isEqualTo(published.licenseNote());
    }

    @Test
    void everyLineEntityIsLinkedBackToItsParentSong() {
        SongEntity entity = mapper.toEntity(TestSongs.draft());

        assertThat(entity.getLines().getFirst().getSong()).isSameAs(entity);
    }
}
