package de.anashabib.content_ingestion.domain.parser;

// 1. Imports your newly created model
import de.anashabib.content_ingestion.domain.model.ParsedLrc;

// 2. Imports the JUnit and Time libraries
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

// 3. Imports the AssertJ testing library for "assertThat"
import static org.assertj.core.api.Assertions.assertThat;

class LrcParserTest {

    private final LrcParser parser = new LrcParser();

    @Test
    void parsesSingleTimestampedLine() {
        String lrc = "[00:15.26]Sie ist ein Model und sie sieht gut aus";

        ParsedLrc result = parser.parse(lrc, Duration.ofSeconds(218));

        assertThat(result.lines()).hasSize(1);
        assertThat(result.lines().getFirst().startMs()).isEqualTo(15_260);
        assertThat(result.lines().getFirst().text())
                .isEqualTo("Sie ist ein Model und sie sieht gut aus");
    }
    @ParameterizedTest
    @CsvSource({
            "'[00:15.2]Text',    15200",   // tenths
            "'[00:15.26]Text',   15260",   // centiseconds
            "'[00:15.263]Text',  15263",   // milliseconds
            "'[01:42.55]Text',  102550",   // minute rollover
            "'[00:15]Text',      15000"    // no fraction at all
    })
    void normalisesAllTimestampPrecisions(String input, int expectedMs) {
        assertThat(parser.parse(input, Duration.ofMinutes(5)).lines().getFirst().startMs())
                .isEqualTo(expectedMs);
    }

}