package de.anashabib.content_ingestion.domain.validation;

import de.anashabib.content_ingestion.domain.model.LrcLine;
import de.anashabib.content_ingestion.domain.model.ParsedLrc;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class LrcValidator {
    // Heuristic rules for data density
    private static final int MIN_LINES = 1;
    private static final long MIN_MS_PER_LINE = 250; // sustained density floor

    public List<ValidationError> validate(ParsedLrc parsedLrc, Duration trackDuration) {
        List<ValidationError> errors = new ArrayList<>();
        List<LrcLine> lines = parsedLrc.lines();
        long durationMs = trackDuration.toMillis();

        validateLineCount(lines, durationMs, errors);
        validateTimestampBounds(lines, durationMs, errors);
        validateStrictlyIncreasing(lines, errors);

        return errors;
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