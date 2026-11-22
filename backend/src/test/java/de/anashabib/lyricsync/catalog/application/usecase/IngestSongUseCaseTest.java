package de.anashabib.lyricsync.catalog.application.usecase;

import de.anashabib.lyricsync.catalog.application.port.out.ArtistRepositoryPort;
import de.anashabib.lyricsync.catalog.application.port.out.AudioStoragePort;
import de.anashabib.lyricsync.catalog.application.port.out.SongRepositoryPort;
import de.anashabib.lyricsync.catalog.domain.encoding.LrcTextDecoder;
import de.anashabib.lyricsync.catalog.domain.model.Artist;
import de.anashabib.lyricsync.catalog.domain.model.Song;
import de.anashabib.lyricsync.catalog.domain.parser.LrcParser;
import de.anashabib.lyricsync.catalog.domain.validation.LrcValidator;
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
        when(audioStoragePort.store(any(), any())).thenReturn("stored/song.mp3");
        when(artistRepositoryPort.findByName("Traditional")).thenReturn(Optional.empty());
        when(artistRepositoryPort.save(any())).thenAnswer(inv ->
                new Artist(UUID.randomUUID(), inv.<Artist>getArgument(0).name()));
        when(songRepositoryPort.save(any())).thenAnswer(inv -> {
            Song s = inv.getArgument(0);
            return new Song(UUID.randomUUID(), s.title(), s.artistId(), s.languageCode(), s.cefrLevel(),
                    s.durationMs(), s.licenseType(), s.audioSourceRef(), s.coverImageUrl(),
                    s.syncGranularity(), s.lines(), Instant.now());
        });

        IngestResult result = useCase.ingest(validCommand("Traditional"));

        assertThat(result).isInstanceOf(IngestResult.Success.class);
        Song saved = ((IngestResult.Success) result).song();
        assertThat(saved.id()).isNotNull();
        assertThat(saved.lines()).hasSize(1);
    }

    @Test
    void reusesAnExistingArtistInsteadOfCreatingADuplicate() {
        when(artistRepositoryPort.findByName("Traditional"))
                .thenReturn(Optional.of(new Artist(UUID.randomUUID(), "Traditional")));
        when(audioStoragePort.store(any(), any())).thenReturn("ref");
        when(songRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.ingest(validCommand("Traditional"));

        verify(artistRepositoryPort, never()).save(any());
    }

    @Test
    void returnsValidationFailureWithoutTouchingStorageOrDatabase() {
        IngestSongCommand command = new IngestSongCommand(
                "audio".getBytes(), "song.mp3",
                "[99:00.00]Way past the end".getBytes(StandardCharsets.UTF_8), null,
                "Test", "Artist", "A2", "public_domain", 10_000 // 10s track, line at 99 minutes
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
                "Test", "Artist", "A2", "public_domain", 20_000
        );

        IngestResult result = useCase.ingest(command);

        assertThat(result).isInstanceOf(IngestResult.ValidationFailure.class);
        verifyNoInteractions(audioStoragePort, songRepositoryPort);
    }

    @Test
    void rejectsRequestWithNeitherLrcFileNorPastedText() {
        IngestSongCommand command = new IngestSongCommand(
                "audio".getBytes(), "song.mp3", null, null,
                "Test", "Artist", "A2", "public_domain", 10_000
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
                "Test", "Artist", "A2", "stolen", 20_000
        );

        assertThat(useCase.ingest(command)).isInstanceOf(IngestResult.ValidationFailure.class);
    }

    private IngestSongCommand validCommand(String artistName) {
        return new IngestSongCommand(
                "audio".getBytes(), "song.mp3",
                "[00:10.00]Line".getBytes(StandardCharsets.UTF_8), null,
                "Test Song", artistName, "A2", "public_domain", 20_000
        );
    }
}