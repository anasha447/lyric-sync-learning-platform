package de.anashabib.lyricsync.catalog.domain.model;

import java.util.UUID;

/** Names are unique ignoring case: "Kraftwerk" and "kraftwerk" are the same artist. */
public record Artist(UUID id, String name) {
    public Artist {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("An artist needs a name");
        }
        name = name.strip();
    }
}
