package de.anashabib.lyricsync.catalog.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest
class FlywayMigrationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void catalogTablesLiveInTheCatalogSchema() {
        List<String> catalogTables = jdbcTemplate.queryForList("""
            SELECT table_name FROM information_schema.tables
            WHERE table_schema = 'catalog'
            """, String.class);
        List<String> publicTables = jdbcTemplate.queryForList("""
            SELECT table_name FROM information_schema.tables
            WHERE table_schema = 'public'
            """, String.class);

        assertThat(catalogTables).containsExactlyInAnyOrder("language", "artist", "song", "song_line");
        assertThat(publicTables).containsOnly("flyway_schema_history");
    }

    // Each test starts from a valid row and breaks one thing, then checks that the expected
    // constraint is what refused it. Otherwise a test could pass because of a different error.
    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', quoteCharacter = '"', value = {
            "language_code = 'xx'                        | songs_language_code_fkey",
            "license_type = 'stolen'                     | song_license_type_check",
            "license_note = ' '                          | song_license_note_not_blank",
            "status = 'DONE'                             | song_status_check",
            "failure_reason = 'boom'                     | song_failure_reason_only_when_failed",
            "status = 'PUBLISHED', published_at = now() | song_published_needs_level",
            "slug = 'Not A Slug'                         | song_slug_format",
            "cefr_level = 'C1'                           | song_cefr_level_check",
            "duration_ms = 0                             | song_duration_positive",
    })
    void rejectsAnInvalidSong(String change, String expectedConstraint) {
        UUID songId = insertTestSong();

        assertThatThrownBy(() -> jdbcTemplate.update("UPDATE catalog.song SET " + change + " WHERE id = ?", songId))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining(expectedConstraint);
    }

    @Test
    void rejectsALineThatDoesNotEndAfterItStarts() {
        UUID songId = insertTestSong();

        assertThatThrownBy(() -> jdbcTemplate.update("""
            INSERT INTO catalog.song_line (id, song_id, line_index, raw_text, start_ms, end_ms)
            VALUES (gen_random_uuid(), ?, 0, 'Zero length', 5000, 5000)
            """, songId))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("song_line_times_check");
    }

    @Test
    void rejectsAnArtistNameThatDiffersOnlyInCase() {
        jdbcTemplate.update("INSERT INTO catalog.artist (id, name) VALUES (gen_random_uuid(), 'Rammstein')");

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO catalog.artist (id, name) VALUES (gen_random_uuid(), 'rammstein')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicateLineIndexWithinASong() {
        UUID songId = insertTestSong();
        jdbcTemplate.update("""
            INSERT INTO catalog.song_line (id, song_id, line_index, raw_text, start_ms, end_ms)
            VALUES (gen_random_uuid(), ?, 0, 'First line', 0, 5000)
            """, songId);

        assertThatThrownBy(() -> jdbcTemplate.update("""
            INSERT INTO catalog.song_line (id, song_id, line_index, raw_text, start_ms, end_ms)
            VALUES (gen_random_uuid(), ?, 0, 'Duplicate index', 5000, 10000)
            """, songId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletingASongCascadesToItsLines() {
        UUID songId = insertTestSong();
        jdbcTemplate.update("""
            INSERT INTO catalog.song_line (id, song_id, line_index, raw_text, start_ms, end_ms)
            VALUES (gen_random_uuid(), ?, 0, 'A line', 0, 5000)
            """, songId);

        jdbcTemplate.update("DELETE FROM catalog.song WHERE id = ?", songId);

        Integer remaining = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM catalog.song_line WHERE song_id = ?", Integer.class, songId);
        assertThat(remaining).isZero();
    }

    private UUID seedLanguageAndArtist() {
        jdbcTemplate.update("INSERT INTO catalog.language (code, name) VALUES ('de', 'German') ON CONFLICT DO NOTHING");
        UUID artistId = UUID.randomUUID();
        // rows here are committed, not rolled back, and artist names must be unique
        jdbcTemplate.update("INSERT INTO catalog.artist (id, name) VALUES (?, ?)", artistId, "Artist " + artistId);
        return artistId;
    }

    private UUID insertTestSong() {
        UUID artistId = seedLanguageAndArtist();
        UUID songId = UUID.randomUUID();
        jdbcTemplate.update("""
            INSERT INTO catalog.song (id, slug, title, artist_id, language_code, duration_ms, status,
                                      license_type, license_note, audio_key)
            VALUES (?, ?, 'Test Song', ?, 'de', 180000, 'DRAFT', 'own_recording', 'Recorded by us', 'audio/test.mp3')
            """, songId, "test-song-" + songId, artistId);
        return songId;
    }
}