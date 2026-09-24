package de.anashabib.content_ingestion.adapter.out.persistence;

import de.anashabib.content_ingestion.adapter.out.persistence.mapper.ArtistMapper;
import de.anashabib.content_ingestion.adapter.out.persistence.mapper.SongMapper;
import de.anashabib.content_ingestion.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Tell Hibernate NOT to overwrite your Flyway database schema constraints!
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SongPersistenceAdapter.class, SongMapper.class, ArtistPersistenceAdapter.class, ArtistMapper.class})
@Testcontainers
class SongPersistenceAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    SongPersistenceAdapter songAdapter;

    @Autowired
    ArtistPersistenceAdapter artistAdapter;

    @Autowired
    SongJpaRepository jpaRepository; // <-- Added to force manual database flushes

    @Test
    void savesAndRetrievesASongWithItsLines() {
        UUID artistId = artistAdapter.save(new Artist(null, "Kraftwerk")).id();

        Song saved = songAdapter.save(songWith(artistId, List.of(
                new SongLine(null, 0, "Sie ist ein Model", 15_260, 19_840, false, null, null),
                new SongLine(null, 1, "und sie sieht gut aus", 19_840, 24_100, false, null, null)
        )));

        Song found = songAdapter.findById(saved.id()).orElseThrow();
        assertThat(found.lines()).hasSize(2);
        assertThat(found.lines().get(0).rawText()).isEqualTo("Sie ist ein Model");
    }

    @Test
    void removingALineOnUpdateActuallyDeletesItFromTheDatabase() {
        UUID artistId = artistAdapter.save(new Artist(null, "Kraftwerk")).id();

        Song original = songAdapter.save(songWith(artistId, List.of(
                new SongLine(null, 0, "Line A", 0, 5000, false, null, null),
                new SongLine(null, 1, "Line B", 5000, 10000, false, null, null)
        )));

        // Flush ensures the original lines are securely locked into the database
        jpaRepository.flush();

        Song updated = new Song(
                original.id(), original.title(), original.artistId(), original.languageCode(),
                original.cefrLevel(), original.durationMs(), original.licenseType(),
                original.audioSourceRef(), original.coverImageUrl(), original.syncGranularity(),
                List.of(original.lines().getFirst()), original.createdAt()
        );

        songAdapter.save(updated);
        jpaRepository.flush(); // Force the delete statement to run before checking

        assertThat(songAdapter.findById(original.id()).orElseThrow().lines()).hasSize(1);
    }

    @Test
    void unknownLanguageCodeStillFailsAtTheDatabaseLevel() {
        UUID artistId = artistAdapter.save(new Artist(null, "Kraftwerk")).id();

        Song song = new Song(
                null, "Test", artistId, "xx", null, 180_000,
                LicenseType.ORIGINAL, "storage/test.mp3", null, SyncGranularity.LINE, List.of(), null
        );

        // We queue the save in memory...
        songAdapter.save(song);

        // ...but we MUST manually flush to force Hibernate to send the INSERT to Postgres.
        assertThatThrownBy(() -> jpaRepository.flush())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Song songWith(UUID artistId, List<SongLine> lines) {
        return new Song(
                null, "Das Model", artistId, "de", "B1", 218_000,
                LicenseType.LICENSED, "storage/das-model.mp3", null, SyncGranularity.LINE, lines, null
        );
    }
}