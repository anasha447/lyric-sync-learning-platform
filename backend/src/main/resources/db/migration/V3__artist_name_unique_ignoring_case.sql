-- "Kraftwerk" and "kraftwerk" are one artist.

UPDATE catalog.artist SET name = btrim(name);

-- Existing duplicates are merged into one row per name (the smallest id), songs follow it.
CREATE TEMPORARY TABLE artist_keeper ON COMMIT DROP AS
SELECT id, first_value(id) OVER (PARTITION BY lower(name) ORDER BY id) AS keeper_id
FROM catalog.artist;

UPDATE catalog.song s
SET artist_id = k.keeper_id
FROM artist_keeper k
WHERE s.artist_id = k.id AND k.id <> k.keeper_id;

DELETE FROM catalog.artist a
USING artist_keeper k
WHERE a.id = k.id AND k.id <> k.keeper_id;

ALTER TABLE catalog.artist ADD CONSTRAINT artist_name_not_blank CHECK (btrim(name) <> '');
CREATE UNIQUE INDEX ux_artist_name_ignoring_case ON catalog.artist (lower(name));
