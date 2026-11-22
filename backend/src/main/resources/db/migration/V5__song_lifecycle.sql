-- Song lifecycle and the fields publishing needs (spec section 8, ADR-0008, ADR-0011).
-- Every new column is backfilled for rows that already exist, then locked down.

ALTER TABLE catalog.song
    ADD COLUMN slug           VARCHAR(100),
    ADD COLUMN status         VARCHAR(20),
    ADD COLUMN failure_reason TEXT,
    ADD COLUMN version        BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN license_note   TEXT,
    ADD COLUMN published_at   TIMESTAMPTZ;

-- Status: nothing has been analysed yet, so existing songs are drafts.
UPDATE catalog.song SET status = 'DRAFT';
ALTER TABLE catalog.song
    ALTER COLUMN status SET NOT NULL,
    ADD CONSTRAINT song_status_check
        CHECK (status IN ('DRAFT', 'PROCESSING', 'READY_FOR_REVIEW', 'PUBLISHED', 'FAILED')),
    ADD CONSTRAINT song_failure_reason_only_when_failed
        CHECK ((status = 'FAILED') = (failure_reason IS NOT NULL)),
    ADD CONSTRAINT song_published_at_only_when_published
        CHECK ((status = 'PUBLISHED') = (published_at IS NOT NULL)),
    ADD CONSTRAINT song_published_needs_level
        CHECK (status <> 'PUBLISHED' OR cefr_level IS NOT NULL);

-- Slug: built from the title like Slug.fromTitle, plus the start of the id so old rows can't collide.
UPDATE catalog.song SET slug = lower(title);
UPDATE catalog.song SET slug = replace(replace(replace(replace(slug, 'ä', 'ae'), 'ö', 'oe'), 'ü', 'ue'), 'ß', 'ss');
UPDATE catalog.song SET slug = btrim(regexp_replace(slug, '[^a-z0-9]+', '-', 'g'), '-');
UPDATE catalog.song SET slug = rtrim(left(slug, 80), '-');
UPDATE catalog.song SET slug = CASE WHEN slug = '' THEN 'song' ELSE slug END || '-' || left(id::text, 8);
ALTER TABLE catalog.song
    ALTER COLUMN slug SET NOT NULL,
    ADD CONSTRAINT song_slug_unique UNIQUE (slug),
    ADD CONSTRAINT song_slug_format CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$');

-- License: the allowed list from ADR-0008, and a note for every song.
ALTER TABLE catalog.song DROP CONSTRAINT songs_license_type_check;
UPDATE catalog.song SET license_type = 'own_recording'      WHERE license_type = 'original';
UPDATE catalog.song SET license_type = 'written_permission' WHERE license_type = 'licensed';
UPDATE catalog.song SET license_note = '(not recorded: added before license notes existed)';
ALTER TABLE catalog.song
    ADD CONSTRAINT song_license_type_check
        CHECK (license_type IN ('public_domain', 'cc_by', 'cc_by_sa', 'own_recording', 'written_permission')),
    ALTER COLUMN license_note SET NOT NULL,
    ADD CONSTRAINT song_license_note_not_blank CHECK (btrim(license_note) <> '');

-- Level: A1 to B2 only; anything else that slipped in before is cleared (it's optional until publishing).
UPDATE catalog.song SET cefr_level = upper(cefr_level);
UPDATE catalog.song SET cefr_level = NULL WHERE cefr_level NOT IN ('A1', 'A2', 'B1', 'B2');
ALTER TABLE catalog.song ADD CONSTRAINT song_cefr_level_check CHECK (cefr_level IN ('A1', 'A2', 'B1', 'B2'));

-- What the domain already guarantees, the database guarantees too.
ALTER TABLE catalog.song
    ALTER COLUMN artist_id SET NOT NULL,
    ALTER COLUMN language_code SET NOT NULL,
    ADD CONSTRAINT song_duration_positive CHECK (duration_ms > 0);
ALTER TABLE catalog.song_line
    ADD CONSTRAINT song_line_times_check CHECK (start_ms >= 0 AND end_ms > start_ms);
