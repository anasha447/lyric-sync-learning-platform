package de.anashabib.lyricsync.catalog.application.port.out;

import de.anashabib.lyricsync.catalog.domain.model.AudioKey;

public interface AudioStoragePort {
    /** Stores the audio under its key. Storing the same key again is harmless: same key, same bytes. */
    void store(AudioKey key, byte[] audioBytes);
}
