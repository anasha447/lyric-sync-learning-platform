package de.anashabib.lyricsync.catalog.application.usecase;

import de.anashabib.lyricsync.catalog.application.port.out.ArtistRepositoryPort;
import de.anashabib.lyricsync.catalog.application.port.out.AudioStoragePort;
import de.anashabib.lyricsync.catalog.application.port.out.SongRepositoryPort;
import de.anashabib.lyricsync.catalog.domain.encoding.LrcTextDecoder;
import de.anashabib.lyricsync.catalog.domain.encoding.UndecodableTextException;
import de.anashabib.lyricsync.catalog.domain.model.*;
import de.anashabib.lyricsync.catalog.domain.parser.LrcParser;
import de.anashabib.lyricsync.catalog.domain.validation.LrcValidator;
import de.anashabib.lyricsync.catalog.domain.validation.ValidationError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class IngestSongUseCase {

    private final LrcTextDecoder lrcTextDecoder;
    private final LrcParser lrcParser;
    private final LrcValidator lrcValidator;
    private final AudioStoragePort audioStoragePort;
    private final ArtistRepositoryPort artistRepositoryPort;
    private final SongRepositoryPort songRepositoryPort;

    public IngestResult ingest(IngestSongCommand command) {
        List<ValidationError> errors = new ArrayList<>();

        if (command.lrcBytes() == null && isBlank(command.lrcText())) {
            errors.add(ValidationError.fileLevel("Either an .lrc file or pasted .lrc text is required"));
            return new IngestResult.ValidationFailure(errors);
        }

        String lrcContent = resolveLrcContent(command, errors);
        if (lrcContent == null) {
            return new IngestResult.ValidationFailure(errors); // decoding failed — nothing further to check
        }

        Duration duration = Duration.ofMillis(command.durationMs());
        ParsedLrc parsed = lrcParser.parse(lrcContent, duration);
        errors.addAll(lrcValidator.validate(parsed, duration));

        LicenseType licenseType = parseLicenseType(command.licenseType(), errors);
        if (isBlank(command.artistName())) {
            errors.add(ValidationError.fileLevel("Artist is required"));
        }

        if (!errors.isEmpty()) {
            return new IngestResult.ValidationFailure(errors); // nothing saved — audio, artist, song, none of it
        }

        AudioKey audioKey = AudioKey.ofMp3(command.audioBytes());
        audioStoragePort.store(audioKey, command.audioBytes());
        UUID artistId = resolveArtist(command.artistName()).id();
        Song song = buildSong(command, parsed, duration, licenseType, audioKey, artistId);

        return new IngestResult.Success(songRepositoryPort.save(song));
    }

    private String resolveLrcContent(IngestSongCommand command, List<ValidationError> errors) {
        if (command.lrcBytes() != null) {
            try {
                return lrcTextDecoder.decode(command.lrcBytes()).content();
            } catch (UndecodableTextException e) {
                errors.add(ValidationError.fileLevel("Could not read .lrc file: " + e.getMessage()));
                return null;
            }
        }
        return command.lrcText(); // already a String from a form field — no decoding needed
    }

    private LicenseType parseLicenseType(String raw, List<ValidationError> errors) {
        try {
            return LicenseType.fromDbValue(raw);
        } catch (IllegalArgumentException | NullPointerException e) {
            errors.add(ValidationError.fileLevel("Unknown or missing license type: " + raw));
            return null;
        }
    }

    private Artist resolveArtist(String artistName) {
        // "kraftwerk" finds the existing "Kraftwerk" instead of creating a second artist
        return artistRepositoryPort.findByNameIgnoringCase(artistName.strip())
                .orElseGet(() -> artistRepositoryPort.save(new Artist(null, artistName)));
    }

    private Song buildSong(IngestSongCommand command, ParsedLrc parsed, Duration duration,
                           LicenseType licenseType, AudioKey audioKey, UUID artistId) {
        List<SongLine> lines = IntStream.range(0, parsed.lines().size())
                .mapToObj(i -> {
                    LrcLine line = parsed.lines().get(i);
                    return new SongLine(null, i, line.text(), line.startMs(), line.endMs(),
                            line.instrumental(), null, null); // translation/note: filled by BE-4.x later
                })
                .toList();

        return new Song(
                null, command.title(), artistId, "de", command.cefrLevel(),
                (int) duration.toMillis(), licenseType, audioKey, null,
                SyncGranularity.LINE, lines, null
        );
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}