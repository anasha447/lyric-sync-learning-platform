package de.anashabib.lyricsync.catalog.application.port.out;

public interface AudioStoragePort {
    String store(String originalFilename, byte[] audioBytes);
}