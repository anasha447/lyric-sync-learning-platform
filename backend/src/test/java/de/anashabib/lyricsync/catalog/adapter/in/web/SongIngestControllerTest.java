package de.anashabib.lyricsync.catalog.adapter.in.web;

import de.anashabib.lyricsync.catalog.application.port.in.IngestResult;
import de.anashabib.lyricsync.catalog.application.port.in.IngestSongUseCase;
import de.anashabib.lyricsync.catalog.application.port.out.AudioStorageException;
import de.anashabib.lyricsync.catalog.domain.model.*;
import de.anashabib.lyricsync.catalog.domain.validation.ValidationError;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

    @Test
    void returns400ListingEveryMissingOrInvalidField() throws Exception {
        mockMvc.perform(multipart("/api/admin/songs")
                        .file(new MockMultipartFile("audio", "song.mp3", "audio/mpeg", "ID3 bytes".getBytes()))
                        .param("lrcText", "[00:10.00]Line")
                        .param("cefrLevel", "C2")
                        .param("durationMs", "-5"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", "application/problem+json"))
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder(
                        "title", "artistName", "cefrLevel", "licenseType", "licenseNote", "durationMs")));

        verifyNoInteractions(ingestSongUseCase);
    }

    @Test
    void returns400WhenTheAudioFileIsMissing() throws Exception {
        mockMvc.perform(withoutAudio().param("durationMs", "218000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("audio"))
                .andExpect(jsonPath("$.errors[0].message").value("is required"));
    }

    @Test
    void aDurationThatIsNotANumberIsAFieldErrorNotACrash() throws Exception {
        mockMvc.perform(withoutAudio()
                        .file(new MockMultipartFile("audio", "song.mp3", "audio/mpeg", "ID3 bytes".getBytes()))
                        .param("durationMs", "three minutes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("durationMs"))
                .andExpect(jsonPath("$.errors[0].message").value("has the wrong format"));
    }

    @Test
    void returns413WhenTheUploadIsTooLarge() throws Exception {
        when(ingestSongUseCase.ingest(any())).thenThrow(new MaxUploadSizeExceededException(20L * 1024 * 1024));

        mockMvc.perform(validUpload())
                .andExpect(status().isPayloadTooLarge())
                .andExpect(header().string("Content-Type", "application/problem+json"))
                .andExpect(jsonPath("$.detail").value("The upload is larger than 20 MB"));
    }

    @Test
    void storageFailureIsA500ThatKeepsServerDetailsPrivate() throws Exception {
        when(ingestSongUseCase.ingest(any())).thenThrow(new AudioStorageException(
                "Failed to store audio file: /data/audio/secret", new IOException("No space left on device")));

        mockMvc.perform(validUpload())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Audio storage failed"))
                .andExpect(content().string(not(containsString("/data/audio"))))
                .andExpect(content().string(not(containsString("No space left"))));
    }

    @Test
    void unexpectedErrorsAre500ProblemsThatKeepServerDetailsPrivate() throws Exception {
        when(ingestSongUseCase.ingest(any())).thenThrow(new IllegalStateException("SELECT * FROM catalog.secret"));

        mockMvc.perform(validUpload())
                .andExpect(status().isInternalServerError())
                .andExpect(header().string("Content-Type", "application/problem+json"))
                .andExpect(content().string(not(containsString("catalog.secret"))));
    }

    private MockMultipartHttpServletRequestBuilder withoutAudio() {
        return (MockMultipartHttpServletRequestBuilder) multipart("/api/admin/songs")
                .param("lrcText", "[00:10.00]Line")
                .param("title", "Das Model")
                .param("artistName", "Kraftwerk")
                .param("licenseType", "written_permission")
                .param("licenseNote", "Permission from the label");
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
