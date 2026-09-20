package de.anashabib.content_ingestion.domain.parser;

import de.anashabib.content_ingestion.domain.model.LrcLine;
import de.anashabib.content_ingestion.domain.model.ParsedLrc;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LrcParser {

    private static final Pattern TIMESTAMP =
            Pattern.compile("\\[(\\d{1,3}):([0-5]\\d)(?:[.:](\\d{1,3}))?]");
    public ParsedLrc parse(String raw, Duration trackDuration) {
        List<LrcLine> lines = new ArrayList<>();

        for (String rawLine : raw.split("\\R")) {
            Matcher m = TIMESTAMP.matcher(rawLine);
            if (m.find()) {
                String text = rawLine.substring(m.end()).trim();
                lines.add(new LrcLine(toMillis(m), text));
            }
        }
        return new ParsedLrc(Map.of(), lines);
    }

    private int toMillis(Matcher m) {
        int minutes  = Integer.parseInt(m.group(1));
        int seconds  = Integer.parseInt(m.group(2));
        String frac  = m.group(3);

        int fractionMs = frac == null ? 0
                : frac.length() == 1 ? Integer.parseInt(frac) * 100  // tenths
                : frac.length() == 2 ? Integer.parseInt(frac) * 10   // centiseconds
                : Integer.parseInt(frac);       // milliseconds

        return (minutes * 60 + seconds) * 1000 + fractionMs;
    }
}