package de.anashabib.content_ingestion.domain.parser;

import de.anashabib.content_ingestion.domain.model.LrcLine;
import de.anashabib.content_ingestion.domain.model.ParsedLrc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class LrcParserTest {

    private final LrcParser parser = new LrcParser();

    @Test
    void parsesSingleTimestampedLine() {
        String lrc = "[00:15.26]Sie ist ein Model und sie sieht gut aus";
        ParsedLrc result = parser.parse(lrc, Duration.ofSeconds(218));
        assertThat(result.lines()).hasSize(1);
        assertThat(result.lines().getFirst().startMs()).isEqualTo(15_260);
        assertThat(result.lines().getFirst().text()).isEqualTo("Sie ist ein Model und sie sieht gut aus");
    }

    @ParameterizedTest
    @CsvSource({
            "'[00:15.2]Text',    15200",
            "'[00:15.26]Text',   15260",
            "'[00:15.263]Text',  15263",
            "'[01:42.55]Text',  102550",
            "'[00:15]Text',      15000"
    })
    void normalisesAllTimestampPrecisions(String input, int expectedMs) {
        assertThat(parser.parse(input, Duration.ofMinutes(5)).lines().getFirst().startMs())
                .isEqualTo(expectedMs);
    }

    @Test
    void capturesMetadataTags() {
        String lrc = "[ti:Das Model]\n[ar:Kraftwerk]\n[00:15.26]Sie ist ein Model";
        ParsedLrc result = parser.parse(lrc, Duration.ofMinutes(5));
        assertThat(result.metadata()).containsEntry("ti", "Das Model").containsEntry("ar", "Kraftwerk");
        assertThat(result.lines()).hasSize(1);
    }

    @Test
    void appliesPositiveOffsetFromMetadataToAllTimestamps() {
        String lrc = "[offset:+250]\n[00:15.00]First line\n[00:20.00]Second line";
        ParsedLrc result = parser.parse(lrc, Duration.ofMinutes(5));
        assertThat(result.lines().get(0).startMs()).isEqualTo(15_250);
        assertThat(result.lines().get(1).startMs()).isEqualTo(20_250);
    }

    @Test
    void appliesNegativeOffsetAndClampsAtZero() {
        String lrc = "[offset:-500]\n[00:00.30]Too early to go negative\n[00:01.00]Second line";
        ParsedLrc result = parser.parse(lrc, Duration.ofMinutes(5));
        assertThat(result.lines().get(0).startMs()).isEqualTo(0);
        assertThat(result.lines().get(1).startMs()).isEqualTo(500);
    }

    @Test
    void noOffsetTagLeavesTimestampsUnchanged() {
        ParsedLrc result = parser.parse("[00:15.00]No offset here", Duration.ofMinutes(5));
        assertThat(result.lines().getFirst().startMs()).isEqualTo(15_000);
    }

    // --- NEW TESTS BELOW ---

    @Test
    void expandsRepeatedTimestampsIntoSeparateLines() {
        String lrc = "[00:24.10][01:42.55]Sie wirkt so kühl, an sie kommt niemand ran";
        ParsedLrc result = parser.parse(lrc, Duration.ofMinutes(5));

        assertThat(result.lines()).hasSize(2);
        assertThat(result.lines()).extracting(LrcLine::startMs)
                .containsExactly(24_100, 102_550);
    }

    @Test
    void sortsLinesByStartTimeRegardlessOfInputOrder() {
        String lrc = "[00:20.00]Second chronologically\n[00:10.00]First chronologically";
        ParsedLrc result = parser.parse(lrc, Duration.ofMinutes(5));

        assertThat(result.lines()).extracting(LrcLine::startMs)
                .containsExactly(10_000, 20_000);
    }

    @Test
    void flagsTimestampWithNoTextAsInstrumental() {
        ParsedLrc result = parser.parse("[01:20.00]", Duration.ofMinutes(5));
        assertThat(result.lines().getFirst().instrumental()).isTrue();
    }
}