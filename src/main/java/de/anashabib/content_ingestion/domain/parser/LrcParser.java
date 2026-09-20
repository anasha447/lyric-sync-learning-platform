package de.anashabib.content_ingestion.domain.parser;

import de.anashabib.content_ingestion.domain.model.LrcLine;
import de.anashabib.content_ingestion.domain.model.ParsedLrc;

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
            Matcher m = TIMESTAMP.matcher(rawLine);
            if (m.find()) {
                String text = rawLine.substring(m.end()).trim();
                lines.add(new LrcLine(toMillis(m), text));
                continue;
            }

            Matcher metaMatcher = METADATA.matcher(rawLine);
            if (metaMatcher.find()) {
                String key = metaMatcher.group(1);
                String value = metaMatcher.group(2).trim();
                metadata.put(key, value);
            }
        }
        return new ParsedLrc(metadata, lines);
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
}