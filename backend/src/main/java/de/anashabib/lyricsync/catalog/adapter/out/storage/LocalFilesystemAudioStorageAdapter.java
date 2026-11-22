package de.anashabib.lyricsync.catalog.adapter.out.storage;

import de.anashabib.lyricsync.catalog.application.port.out.AudioStorageException;
import de.anashabib.lyricsync.catalog.application.port.out.AudioStoragePort;
import de.anashabib.lyricsync.catalog.domain.model.AudioKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class LocalFilesystemAudioStorageAdapter implements AudioStoragePort {

    private final Path storageRoot;

    public LocalFilesystemAudioStorageAdapter(@Value("${app.storage.audio-root}") String audioRoot) {
        this.storageRoot = Path.of(audioRoot).toAbsolutePath().normalize();
    }

    @Override
    public void store(AudioKey key, byte[] audioBytes) {
        // AudioKey already refuses "..", this is a second lock on the same door
        Path target = storageRoot.resolve(key.value()).normalize();
        if (!target.startsWith(storageRoot)) {
            throw new AudioStorageException("Audio key points outside the storage root: " + key.value(), null);
        }
        if (Files.exists(target)) {
            return; // the key is the content hash, so the file is already there
        }
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, audioBytes);
        } catch (IOException e) {
            throw new AudioStorageException("Failed to store audio file: " + key.value(), e);
        }
    }
}
