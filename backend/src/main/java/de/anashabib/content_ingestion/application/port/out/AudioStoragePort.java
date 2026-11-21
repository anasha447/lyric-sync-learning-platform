package de.anashabib.content_ingestion.application.port.out;

public interface AudioStoragePort {
    String store(String originalFilename, byte[] audioBytes);
}