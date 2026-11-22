package de.anashabib.lyricsync.catalog.domain.validation;

import de.anashabib.lyricsync.catalog.domain.model.LrcLine;
import de.anashabib.lyricsync.catalog.domain.model.ParsedLrc;
import de.anashabib.lyricsync.catalog.domain.parser.LrcParser;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LrcValidator {
    // Heuristic rules for data density
    private static final int MIN_LINES = 1;
    private static final long MIN_MS_PER_LINE = 250; // sustained density floor

    public List<ValidationError> validate(ParsedLrc parsedLrc, Duration trackDuration) {
        List<ValidationError> errors = new ArrayList<>();
        List<LrcLine> lines = parsedLrc.lines();
        long durationMs = trackDuration.toMillis();

        validateOffset(parsedLrc.metadata(), errors);
        validateLineCount(lines, durationMs, errors);
        validateTimestampBounds(lines, durationMs, errors);
        validateStrictlyIncreasing(lines, errors);

        return errors;
    }

    private void validateOffset(Map<String, String> metadata, List<ValidationError> errors) {
        String offset = metadata.get("offset");
        if (LrcParser.offsetMs(offset).isEmpty()) {
            errors.add(ValidationError.fileLevel(
                    "[offset:" + offset + "] is not a whole number of milliseconds, e.g. [offset:+250]"));
        }
    }

    private void validateLineCount(List<LrcLine> lines, long durationMs, List<ValidationError> errors) {
        if (lines.size() < MIN_LINES) {
            errors.add(ValidationError.fileLevel(
                    "No timestamped lines found — file may be empty or entirely unparseable"));
            return;
        }

        long minPlausibleDurationMs = lines.size() * MIN_MS_PER_LINE;
        if (minPlausibleDurationMs > durationMs) {
            errors.add(ValidationError.fileLevel(
                    lines.size() + " lines is implausibly dense for a " + durationMs
                            + "ms track (under " + MIN_MS_PER_LINE
                            + "ms/line sustained) — check for duplicated or corrupted timestamps"));
        }
    }

    private void validateTimestampBounds(List<LrcLine> lines, long durationMs, List<ValidationError> errors) {
        for (int i = 0; i < lines.size(); i++) {
            LrcLine line = lines.get(i);
            if (line.startMs() < 0) {
                errors.add(ValidationError.forLine(i, "Negative start time: " + line.startMs() + "ms"));
            }
            if (line.startMs() > durationMs) {
                errors.add(ValidationError.forLine(i,
                        "Start time " + line.startMs() + "ms exceeds track duration " + durationMs + "ms"));
            }
        }
    }

    private void validateStrictlyIncreasing(List<LrcLine> lines, List<ValidationError> errors) {
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).startMs() <= lines.get(i - 1).startMs()) {
                errors.add(ValidationError.forLine(i,
                        "Timestamp " + lines.get(i).startMs() + "ms does not strictly follow line "
                                + (i - 1) + "'s " + lines.get(i - 1).startMs() + "ms"));
            }
        }
    }
}