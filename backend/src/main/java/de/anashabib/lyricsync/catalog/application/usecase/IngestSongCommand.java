package de.anashabib.lyricsync.catalog.application.usecase;

public record IngestSongCommand(
        byte[] audioBytes,
        String audioFilename,
        byte[] lrcBytes,
        String lrcText,
        String title,
        String artistName,
        String cefrLevel,
        String licenseType,
        int durationMs
) {}