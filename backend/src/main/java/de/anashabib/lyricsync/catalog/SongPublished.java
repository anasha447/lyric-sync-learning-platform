package de.anashabib.lyricsync.catalog;

import java.util.UUID;

/** A song became visible in the library. Part of the catalog module's public API, so other modules can listen to it. */
public record SongPublished(UUID songId, String slug) {
}
