package de.anashabib.content_ingestion.db;

import org.junit.jupiter.api.Test;
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
    void migrationsCreateExpectedTables() {
        List<String> tables = jdbcTemplate.queryForList("""
            SELECT table_name FROM information_schema.tables
            WHERE table_schema = 'public'
            """, String.class);
        assertThat(tables).contains("languages", "artists", "songs", "song_lines");
    }

    @Test
    void rejectsSongWithUnknownLanguageCode() {
        UUID artistId = seedLanguageAndArtist();
        assertThatThrownBy(() -> jdbcTemplate.update("""
            INSERT INTO songs (id, title, artist_id, language_code, duration_ms, license_type, audio_source_ref)
            VALUES (gen_random_uuid(), 'Test Song', ?, 'xx', 180000, 'original', 'test/path')
            """, artistId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsInvalidLicenseType() {
        UUID artistId = seedLanguageAndArtist();
        assertThatThrownBy(() -> jdbcTemplate.update("""
            INSERT INTO songs (id, title, artist_id, language_code, duration_ms, license_type, audio_source_ref)
            VALUES (gen_random_uuid(), 'Test Song', ?, 'de', 180000, 'stolen', 'test/path')
            """, artistId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicateLineIndexWithinASong() {
        UUID songId = insertTestSong();
        jdbcTemplate.update("""
            INSERT INTO song_lines (id, song_id, line_index, raw_text, start_ms, end_ms)
            VALUES (gen_random_uuid(), ?, 0, 'First line', 0, 5000)
            """, songId);

        assertThatThrownBy(() -> jdbcTemplate.update("""
            INSERT INTO song_lines (id, song_id, line_index, raw_text, start_ms, end_ms)
            VALUES (gen_random_uuid(), ?, 0, 'Duplicate index', 5000, 10000)
            """, songId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletingASongCascadesToItsLines() {
        UUID songId = insertTestSong();
        jdbcTemplate.update("""
            INSERT INTO song_lines (id, song_id, line_index, raw_text, start_ms, end_ms)
            VALUES (gen_random_uuid(), ?, 0, 'A line', 0, 5000)
            """, songId);

        jdbcTemplate.update("DELETE FROM songs WHERE id = ?", songId);

        Integer remaining = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM song_lines WHERE song_id = ?", Integer.class, songId);
        assertThat(remaining).isZero();
    }

    private UUID seedLanguageAndArtist() {
        jdbcTemplate.update("INSERT INTO languages (code, name) VALUES ('de', 'German') ON CONFLICT DO NOTHING");
        UUID artistId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO artists (id, name) VALUES (?, 'Test Artist')", artistId);
        return artistId;
    }

    private UUID insertTestSong() {
        UUID artistId = seedLanguageAndArtist();
        UUID songId = UUID.randomUUID();
        jdbcTemplate.update("""
            INSERT INTO songs (id, title, artist_id, language_code, duration_ms, license_type, audio_source_ref)
            VALUES (?, 'Test Song', ?, 'de', 180000, 'original', 'test/path')
            """, songId, artistId);
        return songId;
    }
}