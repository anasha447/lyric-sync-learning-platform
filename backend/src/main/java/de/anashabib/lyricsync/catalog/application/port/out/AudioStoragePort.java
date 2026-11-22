package de.anashabib.lyricsync.catalog.application.port.out;

import de.anashabib.lyricsync.catalog.domain.model.AudioKey;

public interface AudioStoragePort {
    /**
     * Stores the audio under its key. Storing the same key again is harmless: same key, same bytes.
     *
     * @return true if this call wrote the file, false if it was already there
     */
    boolean store(AudioKey key, byte[] audioBytes);

    /** Removes the audio; nothing happens if it isn't there. */
    void delete(AudioKey key);
}
