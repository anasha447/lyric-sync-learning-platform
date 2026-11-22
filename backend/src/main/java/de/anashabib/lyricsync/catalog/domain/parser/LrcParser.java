package de.anashabib.lyricsync.catalog.domain.parser;

import de.anashabib.lyricsync.catalog.domain.model.LrcLine;
import de.anashabib.lyricsync.catalog.domain.model.ParsedLrc;

import java.util.Comparator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LrcParser {

    // a line time, [mm:ss], [mm:ss.x], [mm:ss.xx] or [mm:ss.xxx]; only counts at the start of a line
    private static final Pattern LINE_TIMESTAMP =
            Pattern.compile("\\s*\\[(\\d{1,3}):([0-5]\\d)(?:[.:](\\d{1,3}))?]");

    // a word timing inside the text: enhanced LRC <mm:ss.xx>, or a [mm:ss.xx] after the text started
    private static final Pattern WORD_TIMESTAMP =
            Pattern.compile("[<\\[]\\d{1,3}:[0-5]\\d(?:[.:]\\d{1,3})?[>\\]]");

    private static final Pattern METADATA =
            Pattern.compile("\\[([a-zA-Z]+):([^]]+)]");

    // whole milliseconds with an optional sign; at most nine digits, so it always fits in an int
    private static final Pattern OFFSET = Pattern.compile("[+-]?\\d{1,9}");

    private static final char BYTE_ORDER_MARK = '﻿';

    public ParsedLrc parse(String raw, Duration trackDuration) {
        List<LrcLine> lines = new ArrayList<>();
        Map<String, String> metadata = new HashMap<>();

        for (String rawLine : withoutByteOrderMark(raw).split("\\R")) {
            Matcher timeMatcher = LINE_TIMESTAMP.matcher(rawLine);
            List<Integer> lineTimestamps = new ArrayList<>();
            int textStartIndex = 0;

            // [00:24.10][01:42.55]Chorus: every leading timestamp is a start time for the same text
            while (timeMatcher.region(textStartIndex, rawLine.length()).lookingAt()) {
                lineTimestamps.add(toMillis(timeMatcher));
                textStartIndex = timeMatcher.end();
            }

            if (!lineTimestamps.isEmpty()) {
                String text = withoutWordTimings(rawLine.substring(textStartIndex));
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

        // [offset:+N] means "show the lyrics N ms sooner", so it's subtracted
        int offset = offsetMs(metadata.get("offset")).orElse(0);

        List<LrcLine> shiftedLines = lines.stream()
                .map(line -> line.shiftedBy(-offset))
                .sorted(Comparator.comparingInt(LrcLine::startMs))
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

    // Java's UTF-8 decoder keeps the BOM, and it would hide the first line's timestamp
    private static String withoutByteOrderMark(String raw) {
        return !raw.isEmpty() && raw.charAt(0) == BYTE_ORDER_MARK ? raw.substring(1) : raw;
    }

    // word-level sync isn't used yet, so word timings are dropped and the text joined back up
    private static String withoutWordTimings(String text) {
        return WORD_TIMESTAMP.matcher(text).replaceAll("").replaceAll("\\s+", " ").strip();
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

    /**
     * The value of an {@code [offset:…]} tag. Missing or blank means 0. Anything that isn't a signed
     * whole number of milliseconds is empty; the parser then ignores it and the validator reports it.
     */
    public static OptionalInt offsetMs(String raw) {
        if (raw == null || raw.isBlank()) {
            return OptionalInt.of(0);
        }
        String value = raw.strip();
        return OFFSET.matcher(value).matches()
                ? OptionalInt.of(Integer.parseInt(value))
                : OptionalInt.empty();
    }
}