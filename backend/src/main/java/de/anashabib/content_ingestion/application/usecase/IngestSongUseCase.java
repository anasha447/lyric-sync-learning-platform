package de.anashabib.content_ingestion.application.usecase;

import de.anashabib.content_ingestion.application.port.out.ArtistRepositoryPort;
import de.anashabib.content_ingestion.application.port.out.AudioStoragePort;
import de.anashabib.content_ingestion.application.port.out.SongRepositoryPort;
import de.anashabib.content_ingestion.domain.encoding.LrcTextDecoder;
import de.anashabib.content_ingestion.domain.encoding.UndecodableTextException;
import de.anashabib.content_ingestion.domain.model.*;
import de.anashabib.content_ingestion.domain.parser.LrcParser;
import de.anashabib.content_ingestion.domain.validation.LrcValidator;
import de.anashabib.content_ingestion.domain.validation.ValidationError;
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

        if (!errors.isEmpty()) {
            return new IngestResult.ValidationFailure(errors); // nothing saved — audio, artist, song, none of it
        }

        String audioRef = audioStoragePort.store(command.audioFilename(), command.audioBytes());
        UUID artistId = resolveArtist(command.artistName()).id();
        Song song = buildSong(command, parsed, duration, licenseType, audioRef, artistId);

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
        // Same case-sensitivity caveat flagged in BE-1.5's test — "Kraftwerk" and
        // "kraftwerk" would create two rows. Known, not a bug, revisit if it matters.
        return artistRepositoryPort.findByName(artistName)
                .orElseGet(() -> artistRepositoryPort.save(new Artist(null, artistName)));
    }

    private Song buildSong(IngestSongCommand command, ParsedLrc parsed, Duration duration,
                           LicenseType licenseType, String audioRef, UUID artistId) {
        List<SongLine> lines = IntStream.range(0, parsed.lines().size())
                .mapToObj(i -> {
                    LrcLine line = parsed.lines().get(i);
                    return new SongLine(null, i, line.text(), line.startMs(), line.endMs(),
                            line.instrumental(), null, null); // translation/note: filled by BE-4.x later
                })
                .toList();

        return new Song(
                null, command.title(), artistId, "de", command.cefrLevel(),
                (int) duration.toMillis(), licenseType, audioRef, null,
                SyncGranularity.LINE, lines, null
        );
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}