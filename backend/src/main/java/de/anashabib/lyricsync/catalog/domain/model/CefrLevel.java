package de.anashabib.lyricsync.catalog.domain.model;

import java.util.Locale;

/** The CEFR difficulty of a song. Required for publishing, optional before that. */
public enum CefrLevel {
    A1, A2, B1, B2;

    public static CefrLevel fromCode(String code) {
        try {
            return valueOf(code.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Unknown level '" + code + "', expected A1, A2, B1 or B2");
        }
    }
}
