package de.anashabib.content_ingestion.application.usecase;

import de.anashabib.content_ingestion.domain.model.Song;
import de.anashabib.content_ingestion.domain.validation.ValidationError;

import java.util.List;

public sealed interface IngestResult {
    record Success(Song song) implements IngestResult {}
    record ValidationFailure(List<ValidationError> errors) implements IngestResult {}
}