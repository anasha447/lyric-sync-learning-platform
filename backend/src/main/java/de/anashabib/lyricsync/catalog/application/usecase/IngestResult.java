package de.anashabib.lyricsync.catalog.application.usecase;

import de.anashabib.lyricsync.catalog.domain.model.Song;
import de.anashabib.lyricsync.catalog.domain.validation.ValidationError;

import java.util.List;

public sealed interface IngestResult {
    record Success(Song song) implements IngestResult {}
    record ValidationFailure(List<ValidationError> errors) implements IngestResult {}
}