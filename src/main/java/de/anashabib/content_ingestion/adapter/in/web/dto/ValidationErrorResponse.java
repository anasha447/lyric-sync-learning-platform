// adapter/in/web/dto/ValidationErrorResponse.java
package de.anashabib.content_ingestion.adapter.in.web.dto;

import de.anashabib.content_ingestion.domain.validation.ValidationError;

import java.util.List;

public record ValidationErrorResponse(List<ValidationError> errors) {}