package de.anashabib.lyricsync.catalog.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * The multipart form. These checks are about the request's shape (present, not too long, a number);
 * whether the content makes a valid song is the use case's job, so it holds for every caller.
 * Lengths match the database columns, so a long title is a 400, not a failed INSERT.
 */
@Getter @Setter
public class IngestSongRequest {

    @NotNull(message = "is required")
    private MultipartFile audio;            // MP3, checked by the use case

    private MultipartFile lrc;              // an .lrc file, or …
    @Size(max = 100_000, message = "is longer than 100000 characters")
    private String lrcText;                 // … the same text pasted

    @NotBlank(message = "is required")
    @Size(max = 255, message = "is longer than 255 characters")
    private String title;

    @NotBlank(message = "is required")
    @Size(max = 255, message = "is longer than 255 characters")
    private String artistName;

    @Pattern(regexp = "(?i)\\s*(A1|A2|B1|B2)?\\s*", message = "must be A1, A2, B1 or B2")
    private String cefrLevel;               // optional until publishing

    @NotBlank(message = "is required")
    private String licenseType;

    @NotBlank(message = "is required")
    @Size(max = 2000, message = "is longer than 2000 characters")
    private String licenseNote;             // source URL, attribution or permission

    @Positive(message = "must be more than 0")
    private int durationMs;
}
