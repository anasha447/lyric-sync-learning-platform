package de.anashabib.lyricsync.catalog.adapter.in.web;

import de.anashabib.lyricsync.catalog.adapter.in.web.dto.PublicSongResponse;
import de.anashabib.lyricsync.catalog.adapter.in.web.dto.SongPageResponse;
import de.anashabib.lyricsync.catalog.application.port.in.BrowseSongsUseCase;
import de.anashabib.lyricsync.catalog.domain.model.CefrLevel;
import de.anashabib.lyricsync.catalog.domain.model.Slug;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

/** The public library: published songs only, the same answer for every learner, so caches may share it. */
@RestController
@RequestMapping("/api/songs")
public class SongController {

    // browsers and the CDN reuse an answer for a minute, then may serve it for ten more while
    // they check in the background (ADR-0011)
    private static final CacheControl SHARED_SHORT = CacheControl.maxAge(Duration.ofSeconds(60))
            .cachePublic()
            .staleWhileRevalidate(Duration.ofMinutes(10));

    private final BrowseSongsUseCase browse;
    private final String audioBaseUrl;

    public SongController(BrowseSongsUseCase browse,
                          @Value("${app.storage.public-base-url}") String audioBaseUrl) {
        this.browse = browse;
        this.audioBaseUrl = audioBaseUrl.replaceAll("/+$", "");
    }

    @GetMapping
    public ResponseEntity<SongPageResponse> list(@RequestParam(required = false) String level,
                                                 @RequestParam(defaultValue = "0") int page) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page can't be negative");
        }
        return ResponseEntity.ok()
                .cacheControl(SHARED_SHORT)
                .body(SongPageResponse.from(browse.listPublished(parseLevel(level), page)));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PublicSongResponse> get(@PathVariable String slug) {
        return browse.findPublished(parseSlug(slug))
                .map(published -> ResponseEntity.ok()
                        // same version, same JSON: Spring answers 304 itself when If-None-Match matches
                        .eTag("v" + published.song().version())
                        .cacheControl(SHARED_SHORT)
                        .body(PublicSongResponse.from(published, audioBaseUrl)))
                .orElseThrow(() -> notFound(slug));
    }

    private static CefrLevel parseLevel(String level) {
        if (level == null || level.isBlank()) {
            return null;
        }
        try {
            return CefrLevel.fromCode(level);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "level must be A1, A2, B1 or B2");
        }
    }

    // a value that can't be a slug can't be a song either: 404, not a validation error
    private static Slug parseSlug(String slug) {
        try {
            return new Slug(slug);
        } catch (IllegalArgumentException e) {
            throw notFound(slug);
        }
    }

    private static ResponseStatusException notFound(String slug) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "No published song '" + slug + "'");
    }
}
