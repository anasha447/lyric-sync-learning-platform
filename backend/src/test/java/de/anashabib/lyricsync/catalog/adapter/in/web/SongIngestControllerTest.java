package de.anashabib.lyricsync.catalog.adapter.in.web;

import de.anashabib.lyricsync.catalog.application.usecase.IngestResult;
import de.anashabib.lyricsync.catalog.application.usecase.IngestSongUseCase;
import de.anashabib.lyricsync.catalog.domain.model.*;
import de.anashabib.lyricsync.catalog.domain.validation.ValidationError;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SongIngestController.class)
class SongIngestControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean IngestSongUseCase ingestSongUseCase;

    @Test
    void returns201WithSavedSongOnSuccess() throws Exception {
        Song saved = TestSongs.draft();
        when(ingestSongUseCase.ingest(any())).thenReturn(new IngestResult.Success(saved));

        mockMvc.perform(multipart("/api/content/admin/songs")
                        .file(new MockMultipartFile("audio", "song.mp3", "audio/mpeg", "bytes".getBytes()))
                        .param("lrcText", "[00:10.00]Line")
                        .param("title", "Test Song")
                        .param("artistName", "Traditional")
                        .param("licenseType", "public_domain")
                        .param("licenseNote", "Public domain, Wikisource")
                        .param("durationMs", "20000"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Das Model"));
    }

    @Test
    void returns422WithErrorsOnValidationFailure() throws Exception {
        when(ingestSongUseCase.ingest(any())).thenReturn(
                new IngestResult.ValidationFailure(List.of(ValidationError.fileLevel("Bad file"))));

        mockMvc.perform(multipart("/api/content/admin/songs")
                        .file(new MockMultipartFile("audio", "song.mp3", "audio/mpeg", "bytes".getBytes()))
                        .param("lrcText", "[00:10.00]Line")
                        .param("title", "Test Song")
                        .param("artistName", "Traditional")
                        .param("licenseType", "public_domain")
                        .param("licenseNote", "Public domain, Wikisource")
                        .param("durationMs", "20000"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].message").value("Bad file"));
    }
}