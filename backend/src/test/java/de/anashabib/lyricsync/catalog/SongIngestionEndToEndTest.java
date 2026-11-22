package de.anashabib.lyricsync.catalog;

import de.anashabib.lyricsync.TestcontainersConfiguration;
import de.anashabib.lyricsync.catalog.application.port.out.SongRepositoryPort;
import de.anashabib.lyricsync.catalog.domain.model.AudioKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The whole upload path with a real Postgres, real transactions and real files. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SongIngestionEndToEndTest {

    @TempDir
    static Path audioRoot;

    @DynamicPropertySource
    static void storeAudioInATempDir(DynamicPropertyRegistry registry) {
        registry.add("app.storage.audio-root", audioRoot::toString);
    }

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @MockitoSpyBean SongRepositoryPort songs; // the real adapter, so one test can bend one answer

    @Test
    void storesTheSongAndItsAudio() throws Exception {
        byte[] audio = mp3("Hänschen klein");

        upload(audio, "Hänschen klein", "Traditional")
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.slug").value("haenschen-klein"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        assertThat(audioRoot.resolve(AudioKey.ofMp3(audio).value())).exists();
        assertThat(countSongs("haenschen-klein")).isOne();
    }

    @Test
    void aSongThatCannotBeSavedLeavesNeitherAudioNorArtistBehind() throws Exception {
        byte[] firstAudio = mp3("first recording");
        upload(firstAudio, "Die Gedanken sind frei", "Traditional").andExpect(status().isCreated());

        // as if a second upload had checked the slug a moment before the first one saved it
        doReturn(false).when(songs).existsBySlug(any());
        byte[] secondAudio = mp3("second recording");
        upload(secondAudio, "Die Gedanken sind frei", "Volkslied")
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal error"));

        assertThat(audioRoot.resolve(AudioKey.ofMp3(secondAudio).value())).doesNotExist(); // cleaned up
        assertThat(audioRoot.resolve(AudioKey.ofMp3(firstAudio).value())).exists();       // untouched
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM catalog.artist WHERE name = 'Volkslied'", Integer.class))
                .as("the artist was saved in the same transaction, so it was rolled back too")
                .isZero();
    }

    private ResultActions upload(byte[] audio, String title, String artist) throws Exception {
        return mockMvc.perform(multipart("/api/admin/songs")
                .file(new MockMultipartFile("audio", "song.mp3", "audio/mpeg", audio))
                .param("lrcText", "[00:01.00]Erste Zeile\n[00:05.00]Zweite Zeile")
                .param("title", title)
                .param("artistName", artist)
                .param("licenseType", "public_domain")
                .param("licenseNote", "Public domain, Wikisource")
                .param("durationMs", "60000"));
    }

    private int countSongs(String slug) {
        return jdbc.queryForObject("SELECT count(*) FROM catalog.song WHERE slug = ?", Integer.class, slug);
    }

    private static byte[] mp3(String content) {
        return ("ID3" + content).getBytes(StandardCharsets.UTF_8);
    }
}
