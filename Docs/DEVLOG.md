# Development Log



## 2026-09-22 — BE-1.2: Encoding Normalization (Latin-1 to UTF-8)

**Built:**
- `LrcTextDecoder` to transcode raw byte arrays into clean `UTF-8` Strings.
- `DetectedEncoding` and `DecodedText` models to preserve encoding context.
- Domain-specific `UndecodableTextException` for completely unreadable files.

**Decided:**
- Detection strictly attempts `UTF-8` first. Because `UTF-8` multi-byte sequences follow strict mathematical rules, wrong guesses reliably crash (`CharacterCodingException`). If it crashes, it safely falls back to `Windows-1252` (a superset of Latin-1).
- Left BOM stripping to `LrcParser` to avoid duplicating the same logic across two layers.

**Broke / learned:**
- _(none)_

**Branch:** `feature/BE-1.2-lrc-encoding`
**Commits:** `hash` feat(encoding): detect and transcode UTF-8/Windows-1252 and reject undecodable input


## 2026-09-21 — BE-1.1: LrcParser completed

- `[offset:±ms]` applied to every timestamp, clamped at zero
- Repeated timestamps on one line expanded into separate lines
- Lines sorted by start time after expansion
- Empty-text timestamps flagged as instrumental
- `end_ms` derived per line — next line's start, or track duration for the last line
- Enhanced LRC inline word tags stripped from line text
- BOM / CRLF / trailing whitespace handled

**Branch:** `feature/BE-1.1-lrc-parser`
**Commits:** <add your commit hash(es) here>

---

## 2026-09-20 — BE-1.1: LrcParser started

- Scaffolded the content-ingestion Spring Boot service (hexagonal: domain/application/adapter/config)
- LrcParser: parses `[mm:ss.xx]` timestamped lines, normalizes tenths/centisecond/millisecond precision to integer ms
- Metadata tags (`[ti:]`, `[ar:]`, `[length:]`) captured into a map instead of being read as lyric lines

**Branch:** `feature/BE-1.1-lrc-parser`
**Commits:** `0a78a92`, `13e67c3`, `8308934`
