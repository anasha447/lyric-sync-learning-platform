package de.anashabib.content_ingestion.domain.parser;

import de.anashabib.content_ingestion.domain.model.LrcLine;
import de.anashabib.content_ingestion.domain.model.ParsedLrc;

import java.util.Comparator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LrcParser {

    private static final Pattern TIMESTAMP =
            Pattern.compile("\\[(\\d{1,3}):([0-5]\\d)(?:[.:](\\d{1,3}))?]");

    private static final Pattern METADATA =
            Pattern.compile("\\[([a-zA-Z]+):([^]]+)]");

    public ParsedLrc parse(String raw, Duration trackDuration) {
        List<LrcLine> lines = new ArrayList<>();
        Map<String, String> metadata = new HashMap<>();

        for (String rawLine : raw.split("\\R")) {
            Matcher timeMatcher = TIMESTAMP.matcher(rawLine);

            // 1. Temporary list to hold all timestamps found on this single line
            List<Integer> lineTimestamps = new ArrayList<>();
            int textStartIndex = 0;

            // 2. The Fix: A while loop to scoop up EVERY timestamp before the text
            while (timeMatcher.find()) {
                lineTimestamps.add(toMillis(timeMatcher));
                textStartIndex = timeMatcher.end(); // Move the text start index forward
            }

            // 3. If we found timestamps, create a separate LrcLine for each one
            if (!lineTimestamps.isEmpty()) {
                String text = rawLine.substring(textStartIndex).trim();
                for (int startMs : lineTimestamps) {
                    lines.add(new LrcLine(startMs, text));
                }
                continue;
            }

            Matcher metaMatcher = METADATA.matcher(rawLine);
            if (metaMatcher.find()) {
                String key = metaMatcher.group(1);
                String value = metaMatcher.group(2).trim();
                metadata.put(key, value);
            }
        }

        int offsetMs = parseOffset(metadata.get("offset"));

        List<LrcLine> shiftedLines = lines.stream()
                .map(line -> line.shiftedBy(offsetMs))
                .sorted(Comparator.comparingInt(LrcLine::startMs)) // <-- ADD THIS LINE
                .toList();

        List<LrcLine> finalLines = new ArrayList<>();
        for (int i = 0; i < shiftedLines.size(); i++) {
            LrcLine current = shiftedLines.get(i);

            // If there is a next line, use its start time. Otherwise, use track duration.
            int endMs = (i + 1 < shiftedLines.size())
                    ? shiftedLines.get(i + 1).startMs()
                    : (int) trackDuration.toMillis();

            finalLines.add(current.withEndMs(endMs));
        }

        return new ParsedLrc(metadata, finalLines);
        }

    private int toMillis(Matcher m) {
        int minutes  = Integer.parseInt(m.group(1));
        int seconds  = Integer.parseInt(m.group(2));
        String frac  = m.group(3);

        int fractionMs = frac == null ? 0
                : frac.length() == 1 ? Integer.parseInt(frac) * 100
                : frac.length() == 2 ? Integer.parseInt(frac) * 10
                : Integer.parseInt(frac);

        return (minutes * 60 + seconds) * 1000 + fractionMs;
    }

    private int parseOffset(String raw) {
        return (raw == null || raw.isBlank()) ? 0 : Integer.parseInt(raw.trim());
    }
}