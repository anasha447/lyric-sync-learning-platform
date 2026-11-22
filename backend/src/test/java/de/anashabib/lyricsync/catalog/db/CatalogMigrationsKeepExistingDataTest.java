package de.anashabib.lyricsync.catalog.db;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fresh databases are easy. This test fills a database the way it looked after V1,
 * then runs the newer migrations over it, like they will run over a real database.
 */
@Testcontainers
class CatalogMigrationsKeepExistingDataTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final UUID KRAFTWERK = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID KRAFTWERK_LOWERCASE = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID HAENSCHEN = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID NO_LETTERS = UUID.fromString("20000000-0000-0000-0000-000000000002");

    @Test
    void rowsFromVersion1SurviveTheCatalogMigrations() {
        migrateTo("1");
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()));

        jdbc.update("INSERT INTO artists (id, name) VALUES (?, 'Kraftwerk')", KRAFTWERK);
        jdbc.update("INSERT INTO artists (id, name) VALUES (?, ' kraftwerk ')", KRAFTWERK_LOWERCASE);
        jdbc.update("""
            INSERT INTO songs (id, title, artist_id, language_code, cefr_level, duration_ms, license_type, audio_source_ref)
            VALUES (?, 'Hänschen klein', ?, 'de', 'b1', 180000, 'original', '3f1c-haenschen.mp3')
            """, HAENSCHEN, KRAFTWERK);
        jdbc.update("""
            INSERT INTO songs (id, title, artist_id, language_code, cefr_level, duration_ms, license_type, audio_source_ref)
            VALUES (?, '!!!', ?, 'de', 'C1', 180000, 'licensed', '9a2b-noise.mp3')
            """, NO_LETTERS, KRAFTWERK_LOWERCASE);

        migrateTo("latest");

        // the two spellings of Kraftwerk became one artist, and both songs point at it
        assertThat(jdbc.queryForList("SELECT name FROM catalog.artist", String.class)).containsExactly("Kraftwerk");
        assertThat(jdbc.queryForObject("SELECT count(DISTINCT artist_id) FROM catalog.song", Integer.class)).isOne();

        Map<String, Object> haenschen = jdbc.queryForMap("SELECT * FROM catalog.song WHERE id = ?", HAENSCHEN);
        assertThat(haenschen)
                .containsEntry("slug", "haenschen-klein-10000000")
                .containsEntry("status", "DRAFT")
                .containsEntry("license_type", "own_recording")
                .containsEntry("cefr_level", "B1")
                .containsEntry("version", 0L)
                .containsEntry("audio_key", "3f1c-haenschen.mp3");
        assertThat((String) haenschen.get("license_note")).startsWith("(not recorded");

        Map<String, Object> noLetters = jdbc.queryForMap("SELECT * FROM catalog.song WHERE id = ?", NO_LETTERS);
        assertThat(noLetters)
                .containsEntry("slug", "song-20000000")
                .containsEntry("license_type", "written_permission")
                .containsEntry("cefr_level", null);
    }

    private static void migrateTo(String version) {
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .target(version)
                .load()
                .migrate();
    }
}
