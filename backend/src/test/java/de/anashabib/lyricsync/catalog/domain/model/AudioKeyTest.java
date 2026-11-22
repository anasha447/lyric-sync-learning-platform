package de.anashabib.lyricsync.catalog.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class AudioKeyTest {

    @Test
    void keyIsTheSha256OfTheFileContent() {
        // sha256("abc"), the standard test vector
        AudioKey key = AudioKey.ofMp3("abc".getBytes(StandardCharsets.US_ASCII));

        assertThat(key.value())
                .isEqualTo("audio/ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad.mp3");
    }

    @Test
    void sameFileGivesTheSameKeyAndAnyChangeGivesANewOne() {
        byte[] audio = {1, 2, 3};

        assertThat(AudioKey.ofMp3(audio)).isEqualTo(AudioKey.ofMp3(new byte[]{1, 2, 3}));
        assertThat(AudioKey.ofMp3(audio)).isNotEqualTo(AudioKey.ofMp3(new byte[]{1, 2, 4}));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "../etc/passwd", "/absolute.mp3", "audio/../x.mp3", "audio/a b.mp3"})
    void rejectsKeysThatCouldEscapeTheStorageRoot(String value) {
        assertThatIllegalArgumentException().isThrownBy(() -> new AudioKey(value));
    }
}
