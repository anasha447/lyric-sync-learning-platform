package de.anashabib.content_ingestion.adapter.out.persistence;

import de.anashabib.content_ingestion.adapter.out.persistence.mapper.ArtistMapper;
import de.anashabib.content_ingestion.domain.model.Artist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ArtistPersistenceAdapter.class, ArtistMapper.class})
@Testcontainers
class ArtistPersistenceAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    ArtistPersistenceAdapter adapter;

    @Test
    void savesAndFindsArtistById() {
        Artist saved = adapter.save(new Artist(null, "Kraftwerk"));
        assertThat(saved.id()).isNotNull();

        assertThat(adapter.findById(saved.id()).orElseThrow().name()).isEqualTo("Kraftwerk");
    }

    @Test
    void findsArtistByExactName() {
        adapter.save(new Artist(null, "Rammstein"));

        assertThat(adapter.findByName("Rammstein")).isPresent();
        assertThat(adapter.findByName("rammstein")).isEmpty();
    }

    @Test
    void returnsEmptyForUnknownId() {
        assertThat(adapter.findById(UUID.randomUUID())).isEmpty();
    }
}