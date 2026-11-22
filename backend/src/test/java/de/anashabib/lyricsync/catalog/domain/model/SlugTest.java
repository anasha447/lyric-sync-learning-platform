package de.anashabib.lyricsync.catalog.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SlugTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Die Gedanken sind frei       | die-gedanken-sind-frei",
            "Hänschen klein               | haenschen-klein",
            "Weißt du, wie viel Sternlein | weisst-du-wie-viel-sternlein",
            "ÜBER ALLE GIPFEL             | ueber-alle-gipfel",
            "Café -- Olé!                 | cafe-ole",
    })
    void turnsTitlesIntoUrlFriendlySlugs(String title, String expected) {
        assertThat(Slug.fromTitle(title).value()).isEqualTo(expected);
    }

    @Test
    void titleWithoutAnyLettersOrDigitsFallsBackToSong() {
        assertThat(Slug.fromTitle("♪ ♫ ?!").value()).isEqualTo("song");
    }

    @Test
    void longTitlesAreCutAtAWordBoundary() {
        Slug slug = Slug.fromTitle("Ein sehr langer Titel ".repeat(10));

        assertThat(slug.value()).hasSizeLessThanOrEqualTo(80).doesNotEndWith("-");
        assertThat(slug.value()).startsWith("ein-sehr-langer-titel-ein");
    }

    @Test
    void numberedVariantForATakenSlug() {
        assertThat(Slug.fromTitle("Das Model").withNumber(2).value()).isEqualTo("das-model-2");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Das-Model", "das model", "-das", "das--model", "../etc"})
    void rejectsValuesThatAreNotSlugs(String value) {
        assertThatIllegalArgumentException().isThrownBy(() -> new Slug(value));
    }
}
