package de.anashabib.content_ingestion.domain.validation;

import de.anashabib.content_ingestion.domain.model.LrcLine;
import de.anashabib.content_ingestion.domain.model.ParsedLrc;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class LrcValidatorTest {
    private final LrcValidator validator = new LrcValidator();

    @Test
    void validFileProducesNoErrors() {
        ParsedLrc lrc = new ParsedLrc(Map.of(), List.of(
                new LrcLine(10_000, "First"),
                new LrcLine(20_000, "Second")
        ));
        assertThat(validator.validate(lrc, Duration.ofSeconds(40))).isEmpty();
    }

    @Test
    void flagsStartTimeExceedingTrackDuration() {
        ParsedLrc lrc = new ParsedLrc(Map.of(), List.of(
                new LrcLine(250_000, "Too late")
        ));
        List<ValidationError> errors = validator.validate(lrc, Duration.ofSeconds(200));

        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().lineIndex()).isEqualTo(0);
        assertThat(errors.getFirst().message()).contains("exceeds track duration");
    }

    @Test
    void flagsNegativeStartTime() {
        ParsedLrc lrc = new ParsedLrc(Map.of(), List.of(
                new LrcLine(-500, "Impossible")
        ));
        assertThat(validator.validate(lrc, Duration.ofMinutes(3)))
                .anyMatch(e -> e.message().contains("Negative start time"));
    }

    @Test
    void flagsTiedOrNonIncreasingTimestamps() {
        ParsedLrc lrc = new ParsedLrc(Map.of(), List.of(
                new LrcLine(10_000, "First"),
                new LrcLine(10_000, "Tied with the line above")
        ));
        assertThat(validator.validate(lrc, Duration.ofMinutes(3)))
                .anyMatch(e -> Integer.valueOf(1).equals(e.lineIndex())
                        && e.message().contains("does not strictly follow"));
    }

    @Test
    void flagsImplausiblyDenseLineCount() {
        List<LrcLine> denseLines = IntStream.range(0, 500)
                .mapToObj(i -> new LrcLine(i * 100, "Line " + i)) // 100ms apart
                .toList();
        ParsedLrc lrc = new ParsedLrc(Map.of(), denseLines);

        assertThat(validator.validate(lrc, Duration.ofSeconds(50))) // 500 lines, 50s track
                .anyMatch(e -> e.message().contains("implausibly dense"));
    }

    @Test
    void doesNotFlagSparseLyricsAsInvalid() {
        ParsedLrc lrc = new ParsedLrc(Map.of(), List.of(
                new LrcLine(30_000, "Only line in a five-minute song")
        ));
        assertThat(validator.validate(lrc, Duration.ofMinutes(5))).isEmpty();
    }

    @Test
    void flagsEmptyLineList() {
        ParsedLrc lrc = new ParsedLrc(Map.of(), List.of());
        assertThat(validator.validate(lrc, Duration.ofMinutes(3)))
                .anyMatch(e -> e.message().contains("No timestamped lines"));
    }
}