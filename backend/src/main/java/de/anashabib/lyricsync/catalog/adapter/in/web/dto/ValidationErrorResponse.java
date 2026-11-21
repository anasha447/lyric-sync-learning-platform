// adapter/in/web/dto/ValidationErrorResponse.java
package de.anashabib.lyricsync.catalog.adapter.in.web.dto;

import de.anashabib.lyricsync.catalog.domain.validation.ValidationError;

import java.util.List;

public record ValidationErrorResponse(List<ValidationError> errors) {}