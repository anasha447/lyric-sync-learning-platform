package de.anashabib.content_ingestion.domain.encoding;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class LrcTextDecoder {

    // Windows-1252, not plain Latin-1/ISO-8859-1: it's what Windows tools
    // actually write, and it's a strict superset for the printable range.
    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    public DecodedText decode(byte[] rawBytes) {
        return tryDecode(rawBytes, StandardCharsets.UTF_8)
                .map(text -> new DecodedText(text, DetectedEncoding.UTF_8))
                .or(() -> tryDecode(rawBytes, WINDOWS_1252)
                        .map(text -> new DecodedText(text, DetectedEncoding.WINDOWS_1252)))
                .orElseThrow(() -> new UndecodableTextException(
                        "Could not decode as UTF-8 or Windows-1252 (" + rawBytes.length + " bytes)"));
    }

    private Optional<String> tryDecode(byte[] bytes, Charset charset) {
        CharsetDecoder decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        try {
            return Optional.of(decoder.decode(ByteBuffer.wrap(bytes)).toString());
        } catch (CharacterCodingException e) {
            return Optional.empty();
        }
    }
}