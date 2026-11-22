package de.anashabib.lyricsync.catalog;

import java.util.UUID;

/** A song was taken out of the library. Part of the catalog module's public API, so other modules can listen to it. */
public record SongUnpublished(UUID songId, String slug) {
}
