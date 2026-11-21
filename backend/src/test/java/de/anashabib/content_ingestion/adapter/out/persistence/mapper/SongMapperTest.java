// test/adapter/out/persistence/mapper/SongMapperTest.java
package de.anashabib.content_ingestion.adapter.out.persistence.mapper;

import de.anashabib.content_ingestion.adapter.out.persistence.entity.SongEntity;
import de.anashabib.content_ingestion.domain.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SongMapperTest {

    private final SongMapper mapper = new SongMapper();

    @Test
    void roundTripsASongWithItsLinesPreservingFieldsAndOrder() {
        Song song = new Song(
                null, "Das Model", UUID.randomUUID(), "de", "B1", 218_000,
                LicenseType.LICENSED, "storage/das-model.mp3", null, SyncGranularity.LINE,
                List.of(
                        new SongLine(null, 0, "Sie ist ein Model", 15_260, 19_840, false, null, null),
                        new SongLine(null, 1, "und sie sieht gut aus", 19_840, 24_100, false, null, null)
                ),
                null
        );

        Song result = mapper.toDomain(mapper.toEntity(song));

        assertThat(result.title()).isEqualTo("Das Model");
        assertThat(result.licenseType()).isEqualTo(LicenseType.LICENSED);
        assertThat(result.lines()).hasSize(2);
        assertThat(result.lines().get(1).lineIndex()).isEqualTo(1);
    }

    @Test
    void everyLineEntityIsLinkedBackToItsParentSong() {
        Song song = new Song(
                null, "Test", UUID.randomUUID(), "de", null, 5000,
                LicenseType.ORIGINAL, "storage/test.mp3", null, SyncGranularity.LINE,
                List.of(new SongLine(null, 0, "Test line", 0, 5000, false, null, null)),
                null
        );

        SongEntity entity = mapper.toEntity(song);

        assertThat(entity.getLines().getFirst().getSong()).isSameAs(entity);
    }
}