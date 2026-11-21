package de.anashabib.lyricsync.catalog.adapter.out.storage;

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
    void storesFileAndReturnsARetrievableReference() throws IOException {
        byte[] audio = "fake audio bytes".getBytes();

        String ref = adapter.store("song.mp3", audio);

        assertThat(Files.readAllBytes(tempDir.resolve(ref))).isEqualTo(audio);
    }

    @Test
    void sanitizesPathTraversalAttemptsInFilename() {
        String ref = adapter.store("../../../etc/passwd", "harmless".getBytes());

        assertThat(ref).doesNotContain("..");
        assertThat(tempDir.resolve(ref)).exists(); // landed inside tempDir, didn't escape it
    }

    @Test
    void twoUploadsWithTheSameOriginalFilenameDoNotCollide() {
        String ref1 = adapter.store("song.mp3", "first".getBytes());
        String ref2 = adapter.store("song.mp3", "second".getBytes());

        assertThat(ref1).isNotEqualTo(ref2); // the UUID prefix guarantees this
    }
}