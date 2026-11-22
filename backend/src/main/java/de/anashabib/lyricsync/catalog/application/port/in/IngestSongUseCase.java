package de.anashabib.lyricsync.catalog.application.port.in;

/** What the catalog offers for adding a song: decode, parse and check the lyrics, store the audio, save a draft. */
public interface IngestSongUseCase {
    IngestResult ingest(IngestSongCommand command);
}
