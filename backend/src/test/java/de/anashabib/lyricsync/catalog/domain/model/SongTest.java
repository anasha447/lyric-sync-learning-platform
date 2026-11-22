package de.anashabib.lyricsync.catalog.domain.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static de.anashabib.lyricsync.catalog.domain.model.TestSongs.line;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class SongTest {

    private static final Instant NOW = Instant.parse("2026-11-22T10:00:00Z");

    @Nested
    class Invariants {

        @Test
        void newSongIsAnUnsavedDraft() {
            Song song = TestSongs.draft();

            assertThat(song.status()).isEqualTo(SongStatus.DRAFT);
            assertThat(song.id()).isNull();
            assertThat(song.version()).isNull();
            assertThat(song.languageCode()).isEqualTo("de");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = "  ")
        void rejectsBlankTitle(String title) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> draftWith(title, "Public domain, Wikisource", List.of(line(0, 0, 5_000, "Hallo"))))
                    .withMessageContaining("title");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = "  ")
        void rejectsMissingLicenseNote(String note) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> draftWith("Hallo", note, List.of(line(0, 0, 5_000, "Hallo"))))
                    .withMessageContaining("license note");
        }

        @Test
        void rejectsASongWithoutLines() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> draftWith("Hallo", "CC0", List.of()))
                    .withMessageContaining("at least one line");
        }

        @Test
        void rejectsLinesThatAreNotInTimeOrder() {
            List<SongLine> lines = List.of(line(0, 5_000, 9_000, "Zweite"), line(1, 1_000, 5_000, "Erste"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> draftWith("Hallo", "CC0", lines))
                    .withMessageContaining("after line 0");
        }

        @Test
        void rejectsGapsInTheLineNumbering() {
            List<SongLine> lines = List.of(line(0, 0, 5_000, "Eins"), line(2, 5_000, 9_000, "Drei"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> draftWith("Hallo", "CC0", lines))
                    .withMessageContaining("index 2 at position 1");
        }

        @Test
        void rejectsALineThatEndsAfterTheTrack() {
            List<SongLine> lines = List.of(line(0, 0, 300_000, "Zu lang"));

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> draftWith("Hallo", "CC0", lines))
                    .withMessageContaining("after the track ends");
        }

        @Test
        void rejectsAFailureReasonOnASongThatDidNotFail() {
            Song draft = TestSongs.draft();

            assertThatIllegalArgumentException().isThrownBy(() -> new Song(
                    draft.id(), draft.slug(), draft.title(), draft.artistId(), draft.languageCode(),
                    draft.cefrLevel(), draft.durationMs(), SongStatus.DRAFT, "boom",
                    draft.licenseType(), draft.licenseNote(), draft.audioKey(), draft.coverImageUrl(),
                    draft.syncGranularity(), draft.lines(), draft.version(), draft.createdAt(), null));
        }

        @Test
        void linesCannotBeChangedFromOutside() {
            Song song = TestSongs.draft();

            assertThat(song.lines()).isUnmodifiable();
        }

        private Song draftWith(String title, String licenseNote, List<SongLine> lines) {
            return Song.draft(Slug.fromTitle("hallo"), title, UUID.randomUUID(), CefrLevel.A1, 218_000,
                    LicenseType.CC_BY, licenseNote, new AudioKey("audio/hallo.mp3"), lines);
        }
    }

    @Nested
    class Lifecycle {

        @Test
        void aSongGoesFromDraftToPublished() {
            Song published = TestSongs.draft()
                    .submitForAnalysis()
                    .analysisSucceeded()
                    .publish(NOW);

            assertThat(published.status()).isEqualTo(SongStatus.PUBLISHED);
            assertThat(published.publishedAt()).isEqualTo(NOW);
        }

        @Test
        void failedAnalysisKeepsTheReasonUntilTheNextTry() {
            Song failed = TestSongs.draft().submitForAnalysis().analysisFailed("nlp-service timed out");

            assertThat(failed.status()).isEqualTo(SongStatus.FAILED);
            assertThat(failed.failureReason()).isEqualTo("nlp-service timed out");

            Song retried = failed.submitForAnalysis();
            assertThat(retried.status()).isEqualTo(SongStatus.PROCESSING);
            assertThat(retried.failureReason()).isNull();
        }

        @Test
        void failingNeedsAReason() {
            Song processing = TestSongs.draft().submitForAnalysis();

            assertThatIllegalArgumentException().isThrownBy(() -> processing.analysisFailed(" "));
        }

        @Test
        void unpublishingTakesTheSongBackToReview() {
            Song unpublished = readyForReview().publish(NOW).unpublish();

            assertThat(unpublished.status()).isEqualTo(SongStatus.READY_FOR_REVIEW);
            assertThat(unpublished.publishedAt()).isNull();
        }

        @Test
        void publishingNeedsALevel() {
            Song noLevel = withoutLevel(readyForReview());

            assertThatIllegalStateException()
                    .isThrownBy(() -> noLevel.publish(NOW))
                    .withMessageContaining("level");
        }

        @Test
        void aDraftCannotBePublishedBeforeItWasAnalysed() {
            assertThatIllegalStateException()
                    .isThrownBy(() -> TestSongs.draft().publish(NOW))
                    .withMessageContaining("DRAFT");
        }

        @Test
        void analysisResultsOnlyApplyToASongThatIsProcessing() {
            assertThatIllegalStateException().isThrownBy(() -> TestSongs.draft().analysisSucceeded());
            assertThatIllegalStateException().isThrownBy(() -> readyForReview().analysisFailed("late"));
        }

        @Test
        void transitionsKeepEverythingElse() {
            Song draft = TestSongs.draft();
            Song processing = draft.submitForAnalysis();

            assertThat(processing).usingRecursiveComparison()
                    .ignoringFields("status")
                    .isEqualTo(draft);
        }

        private Song readyForReview() {
            return TestSongs.draft().submitForAnalysis().analysisSucceeded();
        }

        private Song withoutLevel(Song s) {
            return new Song(s.id(), s.slug(), s.title(), s.artistId(), s.languageCode(), null,
                    s.durationMs(), s.status(), s.failureReason(), s.licenseType(), s.licenseNote(),
                    s.audioKey(), s.coverImageUrl(), s.syncGranularity(), s.lines(), s.version(),
                    s.createdAt(), s.publishedAt());
        }
    }
}
