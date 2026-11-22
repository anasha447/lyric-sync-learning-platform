package de.anashabib.lyricsync.catalog.domain.model;

/**
 * Where a song is in its lifecycle.
 * <pre>
 * DRAFT ──submit──► PROCESSING ──► READY_FOR_REVIEW ──publish──► PUBLISHED
 *   ▲                  │                  ▲                         │
 *   └──── (retry) ── FAILED               └──────── unpublish ──────┘
 * </pre>
 */
public enum SongStatus {
    /** Stored, not yet sent for analysis. */
    DRAFT,
    /** Being analysed (tokens, dictionary links, translations). */
    PROCESSING,
    /** Analysis finished; waits for an admin to fix unmatched tokens and publish. */
    READY_FOR_REVIEW,
    /** Visible in the library. */
    PUBLISHED,
    /** Analysis failed; the song carries the reason. */
    FAILED
}
