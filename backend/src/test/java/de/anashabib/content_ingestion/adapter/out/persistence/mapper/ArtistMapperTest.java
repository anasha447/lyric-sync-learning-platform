package de.anashabib.content_ingestion.adapter.out.persistence.mapper;

import de.anashabib.content_ingestion.adapter.out.persistence.entity.ArtistEntity;
import de.anashabib.content_ingestion.domain.model.Artist;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArtistMapperTest {

    private final ArtistMapper mapper = new ArtistMapper();

    @Test
    void roundTripsANewArtistWithNullId() {
        Artist artist = new Artist(null, "Kraftwerk");

        ArtistEntity entity = mapper.toEntity(artist);

        assertThat(entity.getId()).isNull(); // JPA assigns this on insert, not the mapper
        assertThat(mapper.toDomain(entity).name()).isEqualTo("Kraftwerk");
    }

    @Test
    void roundTripsAnExistingArtistWithId() {
        Artist artist = new Artist(UUID.randomUUID(), "Kraftwerk");

        assertThat(mapper.toDomain(mapper.toEntity(artist))).isEqualTo(artist);
    }
}