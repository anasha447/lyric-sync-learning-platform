package de.anashabib.lyricsync.catalog.application.port.out;

/** Part of the storage port's contract: any adapter throws this when it can't store or delete audio. */
public class AudioStorageException extends RuntimeException {
    public AudioStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}