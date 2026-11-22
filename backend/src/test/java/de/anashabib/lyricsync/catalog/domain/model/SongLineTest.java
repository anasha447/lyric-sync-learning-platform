package de.anashabib.lyricsync.catalog.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SongLineTest {

    @Test
    void rejectsNegativeStart() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new SongLine(null, 0, "Hallo", -1, 5_000, false, null, null));
    }

    @Test
    void rejectsALineThatDoesNotEndAfterItStarts() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new SongLine(null, 3, "Hallo", 5_000, 5_000, false, null, null))
                .withMessageContaining("Line 3");
    }

    @Test
    void rejectsMissingText() {
        // an instrumental line has empty text, never null
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new SongLine(null, 0, null, 0, 5_000, true, null, null));
    }
}
