CREATE TABLE languages (
    code VARCHAR(5) PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE artists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL
);

CREATE TABLE songs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    artist_id UUID REFERENCES artists(id),
    language_code VARCHAR(5) REFERENCES languages(code),
    cefr_level VARCHAR(2),
    duration_ms INT NOT NULL,
    license_type VARCHAR(30) NOT NULL
        CHECK (license_type IN ('public_domain', 'cc_by', 'original', 'licensed')),
    audio_source_ref TEXT NOT NULL,
    cover_image_url TEXT,
    sync_granularity VARCHAR(10) NOT NULL DEFAULT 'line'
        CHECK (sync_granularity IN ('line', 'word')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE song_lines (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    song_id UUID NOT NULL REFERENCES songs(id) ON DELETE CASCADE,
    line_index INT NOT NULL,
    raw_text TEXT NOT NULL,
    start_ms INT NOT NULL,
    end_ms INT NOT NULL,
    is_instrumental BOOLEAN NOT NULL DEFAULT FALSE,
    contextual_translation TEXT,
    cultural_note TEXT,
    UNIQUE (song_id, line_index)
);

CREATE INDEX idx_song_lines_playback ON song_lines (song_id, start_ms);

INSERT INTO languages (code, name) VALUES
    ('en', 'English'),
    ('de', 'German');