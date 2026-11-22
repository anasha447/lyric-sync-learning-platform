package de.anashabib.lyricsync.catalog.adapter.out.persistence;

import de.anashabib.lyricsync.catalog.adapter.out.persistence.mapper.ArtistMapper;
import de.anashabib.lyricsync.catalog.adapter.out.persistence.mapper.SongMapper;
import de.anashabib.lyricsync.catalog.application.port.in.SongPage;
import de.anashabib.lyricsync.catalog.application.port.in.SongSummary;
import de.anashabib.lyricsync.catalog.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PublishedSongQueryAdapter.class, SongPersistenceAdapter.class, SongMapper.class,
        ArtistPersistenceAdapter.class, ArtistMapper.class})
@Testcontainers
class PublishedSongQueryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired PublishedSongQueryAdapter query;
    @Autowired SongPersistenceAdapter songs;
    @Autowired ArtistPersistenceAdapter artists;

    private UUID traditional;

    @BeforeEach
    void someSongsInTheCatalog() {
        traditional = artists.save(new Artist(null, "Traditional")).id();
        songs.save(TestSongs.published(traditional, "Hänschen klein", CefrLevel.A1));
        songs.save(TestSongs.published(traditional, "Alle meine Entchen", CefrLevel.A1));
        songs.save(TestSongs.published(traditional, "Die Gedanken sind frei", CefrLevel.B1));
        songs.save(TestSongs.draft(traditional, TestSongs.draft().lines())); // "Das Model", not published
    }

    @Test
    void listsOnlyPublishedSongsByTitle() {
        SongPage<SongSummary> page = query.findPublished(null, 0, 20);

        assertThat(page.items()).extracting(SongSummary::title)
                .containsExactly("Alle meine Entchen", "Die Gedanken sind frei", "Hänschen klein");
        assertThat(page.items().getFirst().artistName()).isEqualTo("Traditional");
        assertThat(page.totalItems()).isEqualTo(3);
    }

    @Test
    void filtersByLevel() {
        assertThat(query.findPublished(CefrLevel.A1, 0, 20).items()).extracting(SongSummary::slug)
                .containsExactly("alle-meine-entchen", "haenschen-klein");
        assertThat(query.findPublished(CefrLevel.B2, 0, 20).items()).isEmpty();
    }

    @Test
    void pagesThroughTheList() {
        SongPage<SongSummary> second = query.findPublished(null, 1, 2);

        assertThat(second.items()).extracting(SongSummary::title).containsExactly("Hänschen klein");
        assertThat(second.totalItems()).isEqualTo(3);
        assertThat(second.totalPages()).isEqualTo(2);
    }

    @Test
    void findsAPublishedSongWithItsArtistAndLines() {
        var found = query.findPublishedBySlug(new Slug("haenschen-klein")).orElseThrow();

        assertThat(found.artistName()).isEqualTo("Traditional");
        assertThat(found.song().lines()).hasSize(2);
    }

    @Test
    void aDraftIsNotFoundEvenWithItsSlug() {
        assertThat(query.findPublishedBySlug(new Slug("das-model"))).isEmpty();
    }
}
