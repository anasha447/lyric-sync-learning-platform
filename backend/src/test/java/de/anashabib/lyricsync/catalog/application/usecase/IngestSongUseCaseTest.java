package de.anashabib.lyricsync.catalog.application.usecase;

import de.anashabib.lyricsync.catalog.application.port.out.ArtistRepositoryPort;
import de.anashabib.lyricsync.catalog.application.port.out.AudioStoragePort;
import de.anashabib.lyricsync.catalog.application.port.out.SongRepositoryPort;
import de.anashabib.lyricsync.catalog.domain.encoding.LrcTextDecoder;
import de.anashabib.lyricsync.catalog.domain.model.Artist;
import de.anashabib.lyricsync.catalog.domain.model.AudioKey;
import de.anashabib.lyricsync.catalog.domain.model.CefrLevel;
import de.anashabib.lyricsync.catalog.domain.model.Slug;
import de.anashabib.lyricsync.catalog.domain.model.Song;
import de.anashabib.lyricsync.catalog.domain.model.SongStatus;
import de.anashabib.lyricsync.catalog.domain.parser.LrcParser;
import de.anashabib.lyricsync.catalog.domain.validation.LrcValidator;
import de.anashabib.lyricsync.catalog.domain.validation.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestSongUseCaseTest {

    @Mock AudioStoragePort audioStoragePort;
    @Mock ArtistRepositoryPort artistRepositoryPort;
    @Mock SongRepositoryPort songRepositoryPort;

    private IngestSongUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new IngestSongUseCase(
                new LrcTextDecoder(), new LrcParser(), new LrcValidator(), // real — pure, already tested
                audioStoragePort, artistRepositoryPort, songRepositoryPort  // mocked — I/O boundaries
        );
    }

    @Test
    void savesAValidSongAndReturnsSuccess() {
        when(artistRepositoryPort.findByNameIgnoringCase("Traditional")).thenReturn(Optional.empty());
        when(artistRepositoryPort.save(any())).thenAnswer(inv ->
                new Artist(UUID.randomUUID(), inv.<Artist>getArgument(0).name()));
        when(songRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        IngestResult result = useCase.ingest(validCommand("Traditional"));

        assertThat(result).isInstanceOf(IngestResult.Success.class);
        Song saved = ((IngestResult.Success) result).song();
        assertThat(saved.status()).isEqualTo(SongStatus.DRAFT);
        assertThat(saved.slug()).isEqualTo(new Slug("test-song"));
        assertThat(saved.cefrLevel()).isEqualTo(CefrLevel.A2);
        assertThat(saved.licenseNote()).isEqualTo("Public domain, Wikisource");
        assertThat(saved.lines()).hasSize(1);
    }

    @Test
    void storesTheAudioUnderItsContentHash() {
        when(artistRepositoryPort.findByNameIgnoringCase("Traditional"))
                .thenReturn(Optional.of(new Artist(UUID.randomUUID(), "Traditional")));
        when(songRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        IngestSongCommand command = validCommand("Traditional");

        Song saved = ((IngestResult.Success) useCase.ingest(command)).song();

        AudioKey expected = AudioKey.ofMp3(command.audioBytes());
        verify(audioStoragePort).store(expected, command.audioBytes());
        assertThat(saved.audioKey()).isEqualTo(expected);
    }

    @Test
    void reusesAnExistingArtistInsteadOfCreatingADuplicate() {
        when(artistRepositoryPort.findByNameIgnoringCase("Traditional"))
                .thenReturn(Optional.of(new Artist(UUID.randomUUID(), "Traditional")));
        when(songRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.ingest(validCommand("Traditional"));

        verify(artistRepositoryPort, never()).save(any());
    }

    @Test
    void looksUpTheArtistWithoutSurroundingSpaces() {
        when(artistRepositoryPort.findByNameIgnoringCase("Kraftwerk"))
                .thenReturn(Optional.of(new Artist(UUID.randomUUID(), "Kraftwerk")));
        when(songRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.ingest(validCommand("  Kraftwerk "));

        verify(artistRepositoryPort, never()).save(any());
    }

    @Test
    void blankArtistIsAValidationFailureNotACrash() {
        IngestResult result = useCase.ingest(validCommand("   "));

        assertThat(result).isInstanceOf(IngestResult.ValidationFailure.class);
        verifyNoInteractions(audioStoragePort, songRepositoryPort);
    }

    @Test
    void returnsValidationFailureWithoutTouchingStorageOrDatabase() {
        IngestSongCommand command = new IngestSongCommand(
                "audio".getBytes(), "song.mp3",
                "[99:00.00]Way past the end".getBytes(StandardCharsets.UTF_8), null,
                "Test", "Artist", "A2", "public_domain", "Public domain, Wikisource", 10_000 // 10s track, line at 99 minutes
        );

        IngestResult result = useCase.ingest(command);

        assertThat(result).isInstanceOf(IngestResult.ValidationFailure.class);
        verifyNoInteractions(audioStoragePort, songRepositoryPort);
    }

    @Test
    void nonNumericOffsetIsAValidationFailureNotACrash() {
        IngestSongCommand command = new IngestSongCommand(
                "audio".getBytes(), "song.mp3",
                "[offset:abc]\n[00:10.00]Line".getBytes(StandardCharsets.UTF_8), null,
                "Test", "Artist", "A2", "public_domain", "Public domain, Wikisource", 20_000
        );

        IngestResult result = useCase.ingest(command);

        assertThat(result).isInstanceOf(IngestResult.ValidationFailure.class);
        verifyNoInteractions(audioStoragePort, songRepositoryPort);
    }

    @Test
    void rejectsRequestWithNeitherLrcFileNorPastedText() {
        IngestSongCommand command = new IngestSongCommand(
                "audio".getBytes(), "song.mp3", null, null,
                "Test", "Artist", "A2", "public_domain", "Public domain, Wikisource", 10_000
        );

        IngestResult result = useCase.ingest(command);

        assertThat(result).isInstanceOf(IngestResult.ValidationFailure.class);
        verifyNoInteractions(audioStoragePort, songRepositoryPort);
    }

    @Test
    void rejectsUnknownLicenseType() {
        IngestSongCommand command = new IngestSongCommand(
                "audio".getBytes(), "song.mp3",
                "[00:10.00]Line".getBytes(StandardCharsets.UTF_8), null,
                "Test", "Artist", "A2", "stolen", "Found it online", 20_000
        );

        assertThat(useCase.ingest(command)).isInstanceOf(IngestResult.ValidationFailure.class);
    }

    @Test
    void takesTheNextFreeSlugWhenTheTitleIsTaken() {
        when(artistRepositoryPort.findByNameIgnoringCase("Traditional"))
                .thenReturn(Optional.of(new Artist(UUID.randomUUID(), "Traditional")));
        when(songRepositoryPort.existsBySlug(new Slug("test-song"))).thenReturn(true);
        when(songRepositoryPort.existsBySlug(new Slug("test-song-2"))).thenReturn(true);
        when(songRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Song saved = ((IngestResult.Success) useCase.ingest(validCommand("Traditional"))).song();

        assertThat(saved.slug()).isEqualTo(new Slug("test-song-3"));
    }

    @Test
    void levelIsOptionalAtUpload() {
        when(artistRepositoryPort.findByNameIgnoringCase("Traditional"))
                .thenReturn(Optional.of(new Artist(UUID.randomUUID(), "Traditional")));
        when(songRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        IngestResult result = useCase.ingest(command("Test Song", "", "Public domain, Wikisource", 20_000));

        assertThat(((IngestResult.Success) result).song().cefrLevel()).isNull();
    }

    @Test
    void reportsEveryMissingOrWrongFieldAtOnce() {
        IngestResult result = useCase.ingest(command(" ", "C2", "", 20_000));

        assertThat(result).isInstanceOf(IngestResult.ValidationFailure.class);
        assertThat(((IngestResult.ValidationFailure) result).errors())
                .extracting(ValidationError::message)
                .anyMatch(m -> m.contains("Title is required"))
                .anyMatch(m -> m.contains("expected A1, A2, B1 or B2"))
                .anyMatch(m -> m.contains("License note is required"));
        verifyNoInteractions(audioStoragePort, songRepositoryPort);
    }

    @Test
    void zeroDurationIsAValidationFailureNotACrash() {
        IngestResult result = useCase.ingest(command("Test Song", "A2", "CC0", 0));

        assertThat(result).isInstanceOf(IngestResult.ValidationFailure.class);
        verifyNoInteractions(audioStoragePort, songRepositoryPort);
    }

    private IngestSongCommand validCommand(String artistName) {
        return new IngestSongCommand(
                "audio".getBytes(), "song.mp3",
                "[00:10.00]Line".getBytes(StandardCharsets.UTF_8), null,
                "Test Song", artistName, "A2", "public_domain", "Public domain, Wikisource", 20_000
        );
    }

    private IngestSongCommand command(String title, String level, String licenseNote, int durationMs) {
        return new IngestSongCommand(
                "audio".getBytes(), "song.mp3",
                "[00:00.00]Line".getBytes(StandardCharsets.UTF_8), null,
                title, "Traditional", level, "public_domain", licenseNote, durationMs
        );
    }
}