package de.anashabib.lyricsync.catalog.adapter.in.web;

import de.anashabib.lyricsync.catalog.application.port.in.BrowseSongsUseCase;
import de.anashabib.lyricsync.catalog.application.port.in.PublishedSong;
import de.anashabib.lyricsync.catalog.application.port.in.SongPage;
import de.anashabib.lyricsync.catalog.application.port.in.SongSummary;
import de.anashabib.lyricsync.catalog.domain.model.CefrLevel;
import de.anashabib.lyricsync.catalog.domain.model.Slug;
import de.anashabib.lyricsync.catalog.domain.model.Song;
import de.anashabib.lyricsync.catalog.domain.model.TestSongs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SongController.class)
@TestPropertySource(properties = "app.storage.public-base-url=https://media.example.org/")
class SongControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean BrowseSongsUseCase browse;

    private final Song song = TestSongs.saved(TestSongs.published(UUID.randomUUID(), "Hänschen klein", CefrLevel.A1));

    @Test
    void returnsAPublishedSongWithItsAudioUrlAndCacheHeaders() throws Exception {
        when(browse.findPublished(new Slug("haenschen-klein"))).thenReturn(Optional.of(new PublishedSong(song, "Traditional")));

        mockMvc.perform(get("/api/songs/haenschen-klein"))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"v0\""))
                .andExpect(header().string("Cache-Control", containsString("max-age=60")))
                .andExpect(header().string("Cache-Control", containsString("public")))
                .andExpect(header().string("Cache-Control", containsString("stale-while-revalidate=600")))
                .andExpect(jsonPath("$.title").value("Hänschen klein"))
                .andExpect(jsonPath("$.artist").value("Traditional"))
                .andExpect(jsonPath("$.level").value("A1"))
                .andExpect(jsonPath("$.audioUrl").value("https://media.example.org/audio/haenschen-klein.mp3"))
                .andExpect(jsonPath("$.lines[1].text").value("Zweite Zeile"))
                .andExpect(jsonPath("$.lines[1].startMs").value(5_000));
    }

    @Test
    void answers304WhenTheBrowserAlreadyHasThisVersion() throws Exception {
        when(browse.findPublished(any())).thenReturn(Optional.of(new PublishedSong(song, "Traditional")));

        mockMvc.perform(get("/api/songs/haenschen-klein").header("If-None-Match", "\"v0\""))
                .andExpect(status().isNotModified())
                .andExpect(content().string(""));
    }

    @Test
    void unknownOrUnpublishedSongIs404() throws Exception {
        when(browse.findPublished(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/songs/gibt-es-nicht"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", "application/problem+json"));
    }

    @Test
    void aSlugThatCannotExistIs404NotA500() throws Exception {
        mockMvc.perform(get("/api/songs/Das Model!"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(browse);
    }

    @Test
    void listsPublishedSongsForALevel() throws Exception {
        when(browse.listPublished(CefrLevel.B1, 1)).thenReturn(new SongPage<>(
                List.of(new SongSummary("die-gedanken-sind-frei", "Die Gedanken sind frei", "Traditional", CefrLevel.B1, 60_000)),
                1, 20, 21));

        mockMvc.perform(get("/api/songs").param("level", "b1").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("max-age=60")))
                .andExpect(jsonPath("$.items[0].slug").value("die-gedanken-sind-frei"))
                .andExpect(jsonPath("$.items[0].artist").value("Traditional"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalItems").value(21))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void listsEveryLevelWhenNoneIsGiven() throws Exception {
        when(browse.listPublished(null, 0)).thenReturn(new SongPage<>(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/songs")).andExpect(status().isOk());

        verify(browse).listPublished(null, 0);
    }

    @Test
    void rejectsAnUnknownLevelOrANegativePage() throws Exception {
        mockMvc.perform(get("/api/songs").param("level", "C1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("level must be A1, A2, B1 or B2"));
        mockMvc.perform(get("/api/songs").param("page", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(browse);
    }
}
