package de.anashabib.lyricsync.catalog.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ArtistTest {

    @Test
    void trimsTheName() {
        assertThat(new Artist(null, "  Kraftwerk ").name()).isEqualTo("Kraftwerk");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rejectsBlankName(String name) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Artist(null, name));
    }
}
