package de.anashabib.lyricsync.catalog.domain.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class AudioValidatorTest {

    private final AudioValidator validator = new AudioValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "494433040000",   // "ID3": an ID3v2 tag, how most MP3s start
            "fffb9064",       // MPEG-1 Layer III frame, no tag
            "fff39064",       // MPEG-2 Layer III frame (lower sample rates)
    })
    void acceptsMp3(String firstBytesHex) {
        assertThat(validator.validate(HexFormat.of().parseHex(firstBytesHex))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "fff15080",       // AAC (ADTS): same sync bits, but layer 00, so not an MP3
            "52494646",       // "RIFF": a WAV file
            "4f676753",       // "OggS"
            "3c68746d6c3e",   // "<html>", a file renamed to .mp3
    })
    void rejectsOtherFormatsWhateverTheyAreCalled(String firstBytesHex) {
        assertThat(validator.validate(HexFormat.of().parseHex(firstBytesHex)))
                .extracting(ValidationError::message)
                .containsExactly("The audio file is not an MP3");
    }

    @Test
    void rejectsAnEmptyFile() {
        assertThat(validator.validate(new byte[0]))
                .extracting(ValidationError::message)
                .containsExactly("The audio file is empty");
    }

    @Test
    void acceptsExactly20MbAndRejectsOneByteMore() {
        assertThat(validator.validate(mp3OfSize(AudioValidator.MAX_BYTES))).isEmpty();
        assertThat(validator.validate(mp3OfSize(AudioValidator.MAX_BYTES + 1)))
                .extracting(ValidationError::message)
                .containsExactly("The audio file is larger than 20 MB");
    }

    private static byte[] mp3OfSize(int size) {
        byte[] audio = new byte[size];
        audio[0] = 'I';
        audio[1] = 'D';
        audio[2] = '3';
        return audio;
    }
}
