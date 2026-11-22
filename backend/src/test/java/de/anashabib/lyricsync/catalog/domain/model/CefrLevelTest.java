package de.anashabib.lyricsync.catalog.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CefrLevelTest {

    @Test
    void readsLevelCodesIgnoringCaseAndSpaces() {
        assertThat(CefrLevel.fromCode("A1")).isEqualTo(CefrLevel.A1);
        assertThat(CefrLevel.fromCode(" b2 ")).isEqualTo(CefrLevel.B2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"C1", "A", "beginner", ""})
    void rejectsLevelsOutsideA1ToB2(String code) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> CefrLevel.fromCode(code))
                .withMessageContaining("A1, A2, B1 or B2");
    }
}
