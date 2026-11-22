package de.anashabib.lyricsync.catalog;

import java.util.UUID;

/** A song's tokens or translations changed after analysis ran again. Part of the catalog module's public API, so other modules can listen to it. */
public record SongReanalysed(UUID songId, String slug) {
}
