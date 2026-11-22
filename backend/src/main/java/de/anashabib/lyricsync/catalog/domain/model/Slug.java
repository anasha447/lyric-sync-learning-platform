package de.anashabib.lyricsync.catalog.domain.model;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** The readable, unique part of a song's URL: {@code /songs/die-gedanken-sind-frei}. */
public record Slug(String value) {
    private static final Pattern FORMAT = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");
    private static final int MAX_BASE_LENGTH = 80;
    private static final int MAX_LENGTH = 100; // room for a "-2" … "-999" suffix

    public Slug {
        if (value == null || value.length() > MAX_LENGTH || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("Not a slug: '" + value + "'");
        }
    }

    public static Slug fromTitle(String title) {
        String slug = title.toLowerCase(Locale.GERMAN)
                .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue").replace("ß", "ss");
        slug = Normalizer.normalize(slug, Normalizer.Form.NFD).replaceAll("\\p{M}", ""); // é → e
        slug = slug.replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");

        if (slug.length() > MAX_BASE_LENGTH) {
            slug = slug.substring(0, MAX_BASE_LENGTH);
            int lastDash = slug.lastIndexOf('-');
            slug = lastDash > 0 ? slug.substring(0, lastDash) : slug;
        }
        return new Slug(slug.isEmpty() ? "song" : slug);
    }

    /** "das-model" → "das-model-2", for when the plain slug is already taken. */
    public Slug withNumber(int number) {
        return new Slug(value + "-" + number);
    }
}
