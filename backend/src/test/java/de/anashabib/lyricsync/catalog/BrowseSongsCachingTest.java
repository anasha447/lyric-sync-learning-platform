package de.anashabib.lyricsync.catalog;

import de.anashabib.lyricsync.TestcontainersConfiguration;
import de.anashabib.lyricsync.catalog.application.port.in.BrowseSongsUseCase;
import de.anashabib.lyricsync.catalog.application.port.out.ArtistRepositoryPort;
import de.anashabib.lyricsync.catalog.application.port.out.PublishedSongQueryPort;
import de.anashabib.lyricsync.catalog.application.port.out.SongRepositoryPort;
import de.anashabib.lyricsync.catalog.domain.model.Artist;
import de.anashabib.lyricsync.catalog.domain.model.CefrLevel;
import de.anashabib.lyricsync.catalog.domain.model.Slug;
import de.anashabib.lyricsync.catalog.domain.model.Song;
import de.anashabib.lyricsync.catalog.domain.model.TestSongs;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/** Real Spring context and Postgres: proves the cache sits in front of the database and empties on events. */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class BrowseSongsCachingTest {

    @Autowired BrowseSongsUseCase browse;
    @Autowired SongRepositoryPort songs;
    @Autowired ArtistRepositoryPort artists;
    @Autowired ApplicationEventPublisher events;
    @Autowired CacheManager caches;
    @Autowired MeterRegistry metrics;
    @MockitoSpyBean PublishedSongQueryPort database; // counts how often the cache lets a read through

    @BeforeEach
    void emptyCaches() {
        caches.getCacheNames().forEach(name -> caches.getCache(name).clear());
        clearInvocations(database);
    }

    @Test
    void theSecondReadOfASongComesFromMemory() {
        Slug slug = publish("Kommt ein Vogel geflogen").slug();

        browse.findPublished(slug);
        browse.findPublished(slug);

        verify(database, times(1)).findPublishedBySlug(slug);
    }

    @Test
    void publishingASongEmptiesTheCaches() {
        Song song = publish("Der Mond ist aufgegangen");
        browse.findPublished(song.slug());
        browse.listPublished(CefrLevel.A2, 0);

        events.publishEvent(new SongPublished(song.id(), song.slug().value()));
        browse.findPublished(song.slug());
        browse.listPublished(CefrLevel.A2, 0);

        verify(database, times(2)).findPublishedBySlug(song.slug());
        verify(database, times(2)).findPublished(eq(CefrLevel.A2), eq(0), any(Integer.class));
    }

    @Test
    void aSlugThatDoesNotExistIsNotCached() {
        Slug unknown = new Slug("gibt-es-nicht");

        browse.findPublished(unknown);
        browse.findPublished(unknown);

        verify(database, times(2)).findPublishedBySlug(unknown);
    }

    @Test
    void cacheHitsShowUpInTheMetrics() {
        Slug slug = publish("Weißt du, wie viel Sternlein stehen").slug();
        browse.findPublished(slug);
        browse.findPublished(slug);

        double hits = metrics.get("cache.gets").tag("cache", "publishedSongs").tag("result", "hit")
                .functionCounter().count();
        assertThat(hits).isGreaterThanOrEqualTo(1);
    }

    private Song publish(String title) {
        UUID artistId = artists.findByNameIgnoringCase("Traditional")
                .orElseGet(() -> artists.save(new Artist(null, "Traditional"))).id();
        return songs.save(TestSongs.published(artistId, title, CefrLevel.A2));
    }
}
