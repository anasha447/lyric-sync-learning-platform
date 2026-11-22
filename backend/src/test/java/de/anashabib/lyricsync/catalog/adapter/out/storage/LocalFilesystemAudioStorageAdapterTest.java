package de.anashabib.lyricsync.catalog.adapter.out.storage;

import de.anashabib.lyricsync.catalog.domain.model.AudioKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFilesystemAudioStorageAdapterTest {

    @TempDir Path tempDir; // JUnit creates and cleans this up automatically per test

    private LocalFilesystemAudioStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LocalFilesystemAudioStorageAdapter(tempDir.toString());
    }

    @Test
    void storesTheFileUnderItsKey() throws IOException {
        byte[] audio = "fake audio bytes".getBytes();
        AudioKey key = AudioKey.ofMp3(audio);

        adapter.store(key, audio);

        assertThat(Files.readAllBytes(tempDir.resolve(key.value()))).isEqualTo(audio);
    }

    @Test
    void storingTheSameFileAgainKeepsOneCopy() throws IOException {
        byte[] audio = "same song uploaded twice".getBytes();
        AudioKey key = AudioKey.ofMp3(audio);

        adapter.store(key, audio);
        adapter.store(key, audio);

        try (var files = Files.walk(tempDir)) {
            assertThat(files.filter(Files::isRegularFile)).hasSize(1);
        }
    }
}
