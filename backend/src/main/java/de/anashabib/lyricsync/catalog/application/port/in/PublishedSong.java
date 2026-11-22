package de.anashabib.lyricsync.catalog.application.port.in;

import de.anashabib.lyricsync.catalog.domain.model.Song;

/** A published song with the artist's name, which the song itself only knows by id. */
public record PublishedSong(Song song, String artistName) {
}
