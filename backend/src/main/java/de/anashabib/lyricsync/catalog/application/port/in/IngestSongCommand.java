package de.anashabib.lyricsync.catalog.application.port.in;

public record IngestSongCommand(
        byte[] audioBytes,
        String audioFilename,
        byte[] lrcBytes,
        String lrcText,
        String title,
        String artistName,
        String cefrLevel,
        String licenseType,
        String licenseNote,
        int durationMs
) {}