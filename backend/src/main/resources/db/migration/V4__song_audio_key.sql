-- New uploads are named by their SHA-256 (ADR-0011). Rows from before keep their old
-- file names, which still point at the right file.
ALTER TABLE catalog.song RENAME COLUMN audio_source_ref TO audio_key;
