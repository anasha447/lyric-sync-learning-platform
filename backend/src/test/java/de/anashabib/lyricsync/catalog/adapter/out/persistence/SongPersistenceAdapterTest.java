package de.anashabib.lyricsync.catalog.adapter.out.persistence;

import de.anashabib.lyricsync.catalog.adapter.out.persistence.mapper.ArtistMapper;
import de.anashabib.lyricsync.catalog.adapter.out.persistence.mapper.SongMapper;
import de.anashabib.lyricsync.catalog.domain.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static de.anashabib.lyricsync.catalog.domain.model.TestSongs.line;
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
    SongJpaRepository jpaRepository; // to flush, so the SQL actually reaches Postgres

    @Test
    void savesAndRetrievesASongWithItsLines() {
        Song saved = songAdapter.save(TestSongs.draft(kraftwerk(), List.of(
                line(0, 15_260, 19_840, "Sie ist ein Model"),
                line(1, 19_840, 24_100, "und sie sieht gut aus"))));

        Song found = songAdapter.findById(saved.id()).orElseThrow();
        assertThat(found.lines()).extracting(SongLine::rawText)
                .containsExactly("Sie ist ein Model", "und sie sieht gut aus");
        assertThat(found.status()).isEqualTo(SongStatus.DRAFT);
        assertThat(found.slug()).isEqualTo(new Slug("das-model"));
        assertThat(found.version()).isZero();
    }

    @Test
    void removingALineOnUpdateActuallyDeletesItFromTheDatabase() {
        Song original = songAdapter.save(TestSongs.draft(kraftwerk(), List.of(
                line(0, 0, 5_000, "Line A"),
                line(1, 5_000, 10_000, "Line B"))));
        jpaRepository.flush();

        songAdapter.save(withLines(original, List.of(original.lines().getFirst())));
        jpaRepository.flush(); // force the DELETE to run before checking

        assertThat(songAdapter.findById(original.id()).orElseThrow().lines()).hasSize(1);
    }

    @Test
    void everyUpdateBumpsTheVersion() {
        Song original = songAdapter.save(draftBy(kraftwerk()));
        jpaRepository.flush();

        songAdapter.save(original.submitForAnalysis());
        jpaRepository.flush();

        assertThat(songAdapter.findById(original.id()).orElseThrow().version()).isEqualTo(original.version() + 1);
    }

    @Test
    void savingAStaleCopyFailsInsteadOfOverwritingNewerChanges() {
        Song original = songAdapter.save(draftBy(kraftwerk()));
        jpaRepository.flush();
        songAdapter.save(original.submitForAnalysis()); // someone else got there first
        jpaRepository.flush();

        // still holds the old version number
        assertThatThrownBy(() -> songAdapter.save(original.submitForAnalysis()))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    void findsOutWhetherASlugIsTaken() {
        songAdapter.save(draftBy(kraftwerk()));
        jpaRepository.flush();

        assertThat(songAdapter.existsBySlug(new Slug("das-model"))).isTrue();
        assertThat(songAdapter.existsBySlug(new Slug("das-model-2"))).isFalse();
    }

    @Test
    void databaseRejectsADuplicateSlug() {
        UUID artistId = kraftwerk();
        songAdapter.save(draftBy(artistId));
        jpaRepository.flush();

        songAdapter.save(draftBy(artistId)); // same title, so the same slug

        assertThatThrownBy(() -> jpaRepository.flush())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void unknownLanguageCodeStillFailsAtTheDatabaseLevel() {
        songAdapter.save(withLanguage(TestSongs.draft(kraftwerk(), List.of(line(0, 0, 5_000, "Test"))), "xx"));

        // the INSERT only reaches Postgres on flush
        assertThatThrownBy(() -> jpaRepository.flush())
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Song draftBy(UUID artistId) {
        return TestSongs.draft(artistId, List.of(line(0, 15_260, 19_840, "Sie ist ein Model")));
    }

    private UUID kraftwerk() {
        return artistAdapter.save(new Artist(null, "Kraftwerk")).id();
    }

    private static Song withLines(Song s, List<SongLine> lines) {
        return new Song(s.id(), s.slug(), s.title(), s.artistId(), s.languageCode(), s.cefrLevel(),
                s.durationMs(), s.status(), s.failureReason(), s.licenseType(), s.licenseNote(), s.audioKey(),
                s.coverImageUrl(), s.syncGranularity(), lines, s.version(), s.createdAt(), s.publishedAt());
    }

    private static Song withLanguage(Song s, String languageCode) {
        return new Song(s.id(), s.slug(), s.title(), s.artistId(), languageCode, s.cefrLevel(),
                s.durationMs(), s.status(), s.failureReason(), s.licenseType(), s.licenseNote(), s.audioKey(),
                s.coverImageUrl(), s.syncGranularity(), s.lines(), s.version(), s.createdAt(), s.publishedAt());
    }
}
