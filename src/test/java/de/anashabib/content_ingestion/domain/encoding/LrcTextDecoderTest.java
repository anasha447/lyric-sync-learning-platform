package de.anashabib.content_ingestion.domain.encoding;

import org.junit.jupiter.api.Test;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LrcTextDecoderTest {

    private final LrcTextDecoder decoder = new LrcTextDecoder();

    @Test
    void decodesValidUtf8Correctly() {
        String text = "Grüße aus München – schöne, große Straßen";
        DecodedText result = decoder.decode(text.getBytes(StandardCharsets.UTF_8));

        assertThat(result.content()).isEqualTo(text);
        assertThat(result.encoding()).isEqualTo(DetectedEncoding.UTF_8);
    }

    @Test
    void detectsAndTranscodesWindows1252() {
        String text = "Grüße aus München – schöne, große Straßen";
        byte[] windows1252Bytes = text.getBytes(Charset.forName("windows-1252"));

        DecodedText result = decoder.decode(windows1252Bytes);

        assertThat(result.content()).isEqualTo(text);      // correctly recovered
        assertThat(result.encoding()).isEqualTo(DetectedEncoding.WINDOWS_1252);
    }

    @Test
    void rejectsInputThatIsNeitherValidUtf8NorWindows1252() {
        // 0x81: a lone UTF-8 continuation byte (invalid alone) that is also
        // an unassigned code point in windows-1252 — fails both attempts.
        byte[] corrupt = { (byte) 0x81, 0x41, 0x42 };

        assertThatThrownBy(() -> decoder.decode(corrupt))
                .isInstanceOf(UndecodableTextException.class);
    }
}