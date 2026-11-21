package de.anashabib.content_ingestion.domain.model;

import java.util.List;
import java.util.Map;

public record ParsedLrc(Map<String, String> metadata, List<LrcLine> lines) {}