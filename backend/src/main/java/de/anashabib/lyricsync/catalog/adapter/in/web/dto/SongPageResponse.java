package de.anashabib.lyricsync.catalog.adapter.in.web.dto;

import de.anashabib.lyricsync.catalog.application.port.in.SongPage;
import de.anashabib.lyricsync.catalog.application.port.in.SongSummary;

import java.util.List;

public record SongPageResponse(List<SongSummaryResponse> items, int page, int size, long totalItems, int totalPages) {
    public static SongPageResponse from(SongPage<SongSummary> page) {
        return new SongPageResponse(page.items().stream().map(SongSummaryResponse::from).toList(),
                page.page(), page.size(), page.totalItems(), page.totalPages());
    }
}
