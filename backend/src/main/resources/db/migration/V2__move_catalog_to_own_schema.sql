-- Each module owns its own schema (ADR-0002). Table names follow the spec: singular.
-- SET SCHEMA carries indexes, constraints and column defaults along with each table.
CREATE SCHEMA IF NOT EXISTS catalog;

ALTER TABLE languages  SET SCHEMA catalog;
ALTER TABLE artists    SET SCHEMA catalog;
ALTER TABLE songs      SET SCHEMA catalog;
ALTER TABLE song_lines SET SCHEMA catalog;

ALTER TABLE catalog.languages  RENAME TO language;
ALTER TABLE catalog.artists    RENAME TO artist;
ALTER TABLE catalog.songs      RENAME TO song;
ALTER TABLE catalog.song_lines RENAME TO song_line;

ALTER INDEX catalog.idx_song_lines_playback RENAME TO idx_song_line_playback;
