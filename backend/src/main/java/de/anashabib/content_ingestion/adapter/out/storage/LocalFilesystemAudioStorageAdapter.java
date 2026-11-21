package de.anashabib.content_ingestion.adapter.out.storage;

import de.anashabib.content_ingestion.application.port.out.AudioStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class LocalFilesystemAudioStorageAdapter implements AudioStoragePort {

    private final Path storageRoot;

    public LocalFilesystemAudioStorageAdapter(@Value("${app.storage.audio-root}") String audioRoot) {
        this.storageRoot = Path.of(audioRoot);
    }

    @Override
    public String store(String originalFilename, byte[] audioBytes) {
        try {
            Files.createDirectories(storageRoot);
            String storedFilename = UUID.randomUUID() + "-" + sanitize(originalFilename);
            Files.write(storageRoot.resolve(storedFilename), audioBytes);
            return storedFilename;
        } catch (IOException e) {
            throw new AudioStorageException("Failed to store audio file: " + originalFilename, e);
        }
    }

    private String sanitize(String filename) {
        // Treat the filename like a note handed to you by a stranger: never follow
        // its directions. Path.of(...).getFileName() strips any "../" navigation
        // before we even look at the characters; the regex then removes anything
        // that isn't a plain, boring filename character.
        String base = Path.of(filename).getFileName().toString();
        return base.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    }
}