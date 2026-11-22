package de.anashabib.lyricsync.catalog.adapter.in.web;

import de.anashabib.lyricsync.catalog.adapter.in.web.dto.IngestSongRequest;
import de.anashabib.lyricsync.catalog.adapter.in.web.dto.ValidationErrorResponse;
import de.anashabib.lyricsync.catalog.application.port.in.IngestResult;
import de.anashabib.lyricsync.catalog.application.port.in.IngestSongCommand;
import de.anashabib.lyricsync.catalog.application.port.in.IngestSongUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/content/admin/songs")
@RequiredArgsConstructor
public class SongIngestController {

    private final IngestSongUseCase ingestSongUseCase;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> ingest(@ModelAttribute IngestSongRequest request) throws IOException {
        IngestResult result = ingestSongUseCase.ingest(toCommand(request));

        return switch (result) { // exhaustive — sealed interface, no default needed
            case IngestResult.Success success ->
                    ResponseEntity.status(HttpStatus.CREATED).body(success.song());
            case IngestResult.ValidationFailure failure ->
                    ResponseEntity.unprocessableEntity().body(new ValidationErrorResponse(failure.errors()));
        };
    }

    private IngestSongCommand toCommand(IngestSongRequest request) throws IOException {
        byte[] lrcBytes = (request.getLrc() != null && !request.getLrc().isEmpty())
                ? request.getLrc().getBytes()
                : null;

        return new IngestSongCommand(
                request.getAudio().getBytes(), request.getAudio().getOriginalFilename(),
                lrcBytes, request.getLrcText(), request.getTitle(), request.getArtistName(),
                request.getCefrLevel(), request.getLicenseType(), request.getLicenseNote(),
                request.getDurationMs()
        );
    }
}