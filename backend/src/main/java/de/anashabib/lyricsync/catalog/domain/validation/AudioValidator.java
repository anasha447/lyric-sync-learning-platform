package de.anashabib.lyricsync.catalog.domain.validation;

import java.util.List;

/**
 * Songs are stored and served as MP3 ({@code audio/<sha256>.mp3}), up to 20 MB.
 * The check looks at the bytes: the file name and the Content-Type header are whatever the client says.
 */
public class AudioValidator {

    public static final int MAX_BYTES = 20 * 1024 * 1024;

    public List<ValidationError> validate(byte[] audio) {
        if (audio == null || audio.length == 0) {
            return List.of(ValidationError.fileLevel("The audio file is empty"));
        }
        if (audio.length > MAX_BYTES) {
            return List.of(ValidationError.fileLevel("The audio file is larger than 20 MB"));
        }
        if (!looksLikeMp3(audio)) {
            return List.of(ValidationError.fileLevel("The audio file is not an MP3"));
        }
        return List.of();
    }

    // An MP3 starts with an ID3 tag, or straight away with an audio frame:
    // 11 sync bits (0xFF, then 111xxxxx), and layer bits 01, which mean Layer III.
    // AAC uses the same sync bits with layer 00, which is why the layer is checked too.
    private static boolean looksLikeMp3(byte[] audio) {
        boolean id3Tag = audio.length >= 3 && audio[0] == 'I' && audio[1] == 'D' && audio[2] == '3';
        boolean layer3Frame = audio.length >= 2
                && (audio[0] & 0xFF) == 0xFF
                && (audio[1] & 0xE0) == 0xE0
                && (audio[1] & 0x06) == 0x02;
        return id3Tag || layer3Frame;
    }
}
