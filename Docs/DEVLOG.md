# Development Log


## 2026-09-24 — BE-1.5: Song Aggregate Persistence Adapters

**Built:**
- JPA Entities (`SongEntity`, `SongLineEntity`, `ArtistEntity`) utilizing `CascadeType.ALL` and orphan removal for complete lifecycle management of song lines.
- Custom JPA attribute converters (`LicenseTypeConverter`, `SyncGranularityConverter`) to map domain enums to PostgreSQL string columns.
- Hexagonal Architecture out-ports (`SongPersistenceAdapter`, `ArtistPersistenceAdapter`) and mapping layers to strictly isolate the core domain from the database infrastructure.
- Comprehensive integration test suites (`SongPersistenceAdapterTest`, `ArtistPersistenceAdapterTest`) utilizing `@DataJpaTest` and Testcontainers.

**Decided:**
- Completely disable Hibernate's auto-schema generation in tests (`spring.jpa.hibernate.ddl-auto=none`) to ensure tests strictly validate against the actual Flyway database schema.
- Maintain strict separation between database entities and core domain models, utilizing mappers to prevent JPA annotations from leaking into the domain layer.

**Broke / learned:**
- Hibernate's write-behind optimization delays `INSERT` statements for entities with pre-assigned UUIDs. Combined with the automatic transaction rollback of `@DataJpaTest`, this causes database-level constraint violations to fail silently. You must forcefully trigger `jpaRepository.flush()` in tests to actually execute the SQL and catch `DataIntegrityViolationException`s.

**Branch:** `feature/BE-1.5-persistence-adapter`
## 2026-09-23 — BE-1.4: Database Infrastructure and Baseline Schema

**Built:**
- Local PostgreSQL 16 infrastructure via `docker-compose.yml`.
- Flyway baseline migration (`V1__catalog_schema.sql`) establishing tables and constraints for `languages`, `artists`, `songs`, and `song_lines`.
- Testcontainers 2.0.5 integration for automated ephemeral integration testing.
- `FlywayMigrationIntegrationTest` and updated `ContentIngestionApplicationTests` to verify database connectivity.

**Decided:**
- Apply strict relational constraints and cascading rules at the database schema level before implementing JPA entities.

**Broke / learned:**
- Testcontainers 2.0 introduced major breaking changes, requiring the `testcontainers-` prefix in Maven artifact IDs and removing generic types (`<?>`) from `@Container` declarations.

**Branch:** `feature/BE-1.4-catalog-schema`

---

## 2026-09-23 — BE-1.3: Domain Validation for LRC Parsing

**Built:**
- `LrcValidator` within the `domain.validation` package to enforce strict business rules.
- Validation logic to guarantee chronological timestamp ordering, prevent overlapping timestamps, and reject empty lyric lines.

**Decided:**
- Keep validation strictly isolated in the pure domain layer using strict TDD (JUnit 5 + AssertJ) to guarantee model integrity before data ever hits the persistence layer.

**Broke / learned:**
- *(none)*

**Branch:** `feature/BE-1.3-lrc-validation`

---

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

---

## 2026-09-21 — BE-1.1: LrcParser completed

- `[offset:±ms]` applied to every timestamp, clamped at zero
- Repeated timestamps on one line expanded into separate lines
- Lines sorted by start time after expansion
- Empty-text timestamps flagged as instrumental
- `end_ms` derived per line — next line's start, or track duration for the last line
- Enhanced LRC inline word tags stripped from line text
- BOM / CRLF / trailing whitespace handled

**Branch:** `feature/BE-1.1-lrc-parser`

---

## 2026-09-20 — BE-1.1: LrcParser started

- Scaffolded the content-ingestion Spring Boot service (hexagonal: domain/application/adapter/config)
- LrcParser: parses `[mm:ss.xx]` timestamped lines, normalizes tenths/centisecond/millisecond precision to integer ms
- Metadata tags (`[ti:]`, `[ar:]`, `[length:]`) captured into a map instead of being read as lyric lines

**Branch:** `feature/BE-1.1-lrc-parser`
