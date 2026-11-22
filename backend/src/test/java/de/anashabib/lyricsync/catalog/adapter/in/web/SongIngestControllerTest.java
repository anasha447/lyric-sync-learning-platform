package de.anashabib.lyricsync.catalog.adapter.in.web;

import de.anashabib.lyricsync.catalog.application.port.in.IngestResult;
import de.anashabib.lyricsync.catalog.application.port.in.IngestSongUseCase;
import de.anashabib.lyricsync.catalog.domain.model.*;
import de.anashabib.lyricsync.catalog.domain.validation.ValidationError;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SongIngestController.class)
class SongIngestControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean IngestSongUseCase ingestSongUseCase;

    @Test
    void returns201WithTheSongAndWhereItLives() throws Exception {
        Song saved = TestSongs.saved(TestSongs.draft());
        when(ingestSongUseCase.ingest(any())).thenReturn(new IngestResult.Success(saved));

        mockMvc.perform(validUpload())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/admin/songs/" + saved.id()))
                .andExpect(jsonPath("$.id").value(saved.id().toString()))
                .andExpect(jsonPath("$.slug").value("das-model"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.level").value("B1"))
                .andExpect(jsonPath("$.licenseType").value("written_permission")) // same spelling as the request
                .andExpect(jsonPath("$.audioKey").value("audio/das-model.mp3"))
                .andExpect(jsonPath("$.version").value(0))
                .andExpect(jsonPath("$.lines[0].text").value("Sie ist ein Model"))
                .andExpect(jsonPath("$.lines[0].startMs").value(15_260));
    }

    @Test
    void returns422WithTheProblemsInTheFile() throws Exception {
        when(ingestSongUseCase.ingest(any())).thenReturn(new IngestResult.ValidationFailure(List.of(
                ValidationError.forLine(3, "Timestamp 5000ms does not strictly follow line 2's 6000ms"),
                ValidationError.fileLevel("License note is required"))));

        mockMvc.perform(validUpload())
                .andExpect(status().isUnprocessableContent())
                .andExpect(header().string("Content-Type", "application/problem+json"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.detail").value("The song can't be added: 2 problems"))
                .andExpect(jsonPath("$.errors[0].line").value(3))
                .andExpect(jsonPath("$.errors[0].message").value("Timestamp 5000ms does not strictly follow line 2's 6000ms"))
                .andExpect(jsonPath("$.errors[1].line").doesNotExist())
                .andExpect(jsonPath("$.errors[1].message").value("License note is required"));
    }

    private MockMultipartHttpServletRequestBuilder validUpload() {
        return (MockMultipartHttpServletRequestBuilder) multipart("/api/admin/songs")
                .file(new MockMultipartFile("audio", "song.mp3", "audio/mpeg", "ID3 bytes".getBytes()))
                .param("lrcText", "[00:10.00]Line")
                .param("title", "Das Model")
                .param("artistName", "Kraftwerk")
                .param("cefrLevel", "B1")
                .param("licenseType", "written_permission")
                .param("licenseNote", "Permission from the label")
                .param("durationMs", "218000");
    }
}
