// adapter/in/web/dto/IngestSongRequest.java
package de.anashabib.lyricsync.catalog.adapter.in.web.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class IngestSongRequest {
    private MultipartFile audio;
    private MultipartFile lrc;      // optional — one of lrc/lrcText is required
    private String lrcText;         // optional
    private String title;
    private String artistName;
    private String cefrLevel;
    private String licenseType;
    private int durationMs;
}