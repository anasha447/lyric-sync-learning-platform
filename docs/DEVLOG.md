# Development Log

## 2026-11-22 — BE-3.2: Public song API with caching

- `GET /api/songs?level=&page=` (published only, by title, 20 per page) and `GET /api/songs/{slug}` (lines + `audioUrl`)
- A separate read port, `PublishedSongQueryPort`: the list is one JPQL query joined with the artist, without loading lines
- `ETag: "v<version>"` and `Cache-Control: public, max-age=60, stale-while-revalidate=600`; Spring answers 304 on a matching `If-None-Match`
- Spring Cache + Caffeine (new dependencies, approved): `publishedSongs` and `publishedSongPages`, at most 500 entries, 10 min as a safety net; "not found" isn't cached
- `SongPublished`, `SongUnpublished`, `SongReanalysed` in the catalog API package; caches are emptied after the commit (`@TransactionalEventListener`) so a reader can't put an old row back in between
- Hit and miss counts in Prometheus (`cache_gets_total`)
- Proven with a spy on the query port: the second read doesn't reach the database; removing `@Cacheable` makes exactly those tests fail
- A slug that can't exist (`Das Model!`) is a 404, an unknown level a 400, both as problem+json

**Branch:** `feature/BE-3.2-public-song-api`

---

## 2026-11-22 — BE-2.8: Ingestion hardening

- Use case behind an input port (`IngestSongUseCase` interface, `IngestSongService` implementation); command and result moved to `application/port/in`
- `POST /api/admin/songs` (the spec's route; was `/api/content/admin/songs`): 201 with a `SongResponse` DTO and a relative `Location` header, no more domain records on the wire
- Bean Validation on the form (required fields, lengths matching the columns, level, positive duration); a request without audio used to be a NullPointerException
- One `@RestControllerAdvice`, everything as problem+json: 400 lists every bad field, 413 over 20 MB, 422 lists every problem in the file, 500 logs the error and shows nothing internal
- `AudioStorageException` moved into the storage port; the web adapter may not import the storage adapter (the BE-2.5 architecture test would have failed)
- Only MP3, decided by the first bytes (ID3 tag or a Layer III frame), not by file name or Content-Type; AAC with the same sync bits is rejected
- Audio stored first, then artist + song in one transaction; if that fails, the audio is deleted again, but only if this upload wrote it (content-hash files can be shared). End-to-end test with a real slug clash
- Tried on the stack: 201, 422 for a WAV named .mp3, 400, 413 in 60 ms from inside the Docker network
- Broke / learned: German titles looked broken twice ("M�nnlein"); the database held correct UTF-8. Windows curl sent the command-line text in the ANSI code page, and Python printed to the console in it. Checked at byte level before calling it a bug

**Branch:** `feature/BE-2.8-ingestion-hardening`

---

## 2026-11-22 — BE-2.7: Song lifecycle and catalog schema

- Four migrations: V2 moves the tables into a `catalog` schema with singular names, V3 makes artist names unique ignoring case (existing duplicates merged), V4 renames the audio column to `audio_key`, V5 adds status, slug, version, license note, failure reason and publish date
- Every migration backfills existing rows; `CatalogMigrationsKeepExistingDataTest` fills a V1 database and runs the rest over it
- `Song` checks its invariants in the constructor (title, license note, at least one line, line order and times, failure reason only when FAILED, publish date and level when PUBLISHED); status changes are methods that return a new Song
- New uploads are DRAFT, not PROCESSING: nothing analyses songs until BE-4.4
- Slugs from titles (`Hänschen klein` → `haenschen-klein`, then `-2`, `-3`); audio stored under its SHA-256; `version` is JPA's optimistic lock and the future ETag
- License types now match ADR-0008 (`cc_by_sa` added; `original` → `own_recording`, `licensed` → `written_permission`); a license note is required
- The same rules are database constraints; each negative schema test checks the named constraint, so it can't pass because of a different error
- Broke / learned: Python's `write_text` on Windows writes the ANSI code page, not UTF-8; two comments came out as invalid bytes and javac flagged them

**Branch:** `feature/BE-2.7-song-lifecycle`

---

## 2026-11-22 — BE-2.6: LRC parser fixes

- `[offset:+N]` now shows lyrics N ms sooner, as in the LRC convention; it was applied the wrong way round
- Only timestamps at the start of a line are line times; a `[mm:ss]` later in the line, and enhanced LRC `<mm:ss.xx>` word tags, are word timings and get dropped, so "Sie ist ein [00:12]Model" keeps all its words
- UTF-8 BOM stripped before parsing. It used to slip through only because the old regex searched the whole line; with anchored timestamps it would have silently removed the first line
- Non-numeric offset (`[offset:abc]`) is a file-level validation error (422) instead of a `NumberFormatException` (500)
- Validator rejects a line that doesn't end after it starts (e.g. the last line starting exactly at the end of the track)
- Broke / learned: the BE-1.1 entry lists word tags and BOM as handled, but no test covered them, and the word tags weren't stripped. Every fix here starts with a failing test

**Branch:** `fix/BE-2.6-lrc-parser-fixes`

---

## 2026-11-22 — BE-2.5: Architecture guard tests

- `ModularityTest`: `ApplicationModules.verify()` plus a check that the catalog module is found at all, so verify can't pass on zero modules; writes module docs to `target/spring-modulith-docs`
- `HexagonalArchitectureTest` (restores the lost ArchUnit test): domain depends only on the JDK and itself; application doesn't know adapters or config; inbound and outbound adapters stay apart; entities and controllers stay in their adapters
- Rules apply to every module by package pattern, so dictionary and learning get them for free
- Each rule was proven red with a throwaway violation (Spring and Lombok in the domain, controller → persistence adapter, cross-module call) before committing
- Broke / learned: Lombok annotations are gone after compiling; ArchUnit only sees Lombok through `@lombok.Generated`, now pinned on in `lombok.config` instead of relying on the default
- ArchUnit comes in through Spring Modulith, so no new dependency

**Branch:** `chore/BE-2.5-architecture-guard`

---

## 2026-11-21 — OPS-2.4: Local stack with Docker Compose

- Root `compose.yaml`: Postgres + RabbitMQ by default, Traefik + backend in the `app` profile; DB and broker ports bound to 127.0.0.1 only
- `./mvnw spring-boot:run` starts the infra from the same file via Spring Boot's Docker Compose support
- Actuator: health with readiness/liveness probes, Prometheus metrics tagged `application=lyricsync-backend`; only health, info and prometheus are exposed, and Traefik doesn't route `/actuator`
- springdoc 3.2.0 (built against Boot 4.1.2): Swagger UI at `/swagger-ui.html`
- JSON logs (ECS) in the container through Spring Boot's structured logging, plain text in the IDE
- Shared `TestcontainersConfiguration` for full-context tests; `OperationalEndpointsTest` written first, red → green
- Database renamed `content_ingestion` → `lyricsync`; image gets a writable `/data/audio` for the non-root user

**Branch:** `chore/OPS-2.4-local-stack`
---

## 2026-11-21 — DOC-2.9: Caching strategy

- ADR-0011: cache shared, rarely changing data close to the learner; never cache one learner's data on the server
- Audio switches from presigned URLs to public, content-hashed CDN URLs (`immutable`, one year): a new signature per request would have made the browser download the same MP3 again every visit (ADR-0005 amended)
- Song JSON: TanStack Query + `ETag` + CDN + Caffeine, cleared by publish/re-analyse events; learner data `private, no-store`
- No Redis (single backend instance) and no Hibernate second-level cache
- Spec (new section 7.7, API, data model) and architecture (caching section + diagram) updated

**Branch:** `docs/DOC-2.9-caching`

---

## 2026-11-21 — OPS-2.3b: Keep Dependabot on Java 21 base images

- Dependabot's first run proposed `eclipse-temurin` 21 → 24; closed it and ignored major updates for the base image, so only Java 21 patches come through
- Merged its other two PRs: `actions/setup-java` 6.3.0 and the Maven wrapper 3.10.1

**Branch:** `fix/OPS-2.3b-dependabot-java21`

---

## 2026-11-21 — OPS-2.3: CI with GitHub Actions

- PRs run `./mvnw verify` with Testcontainers on Temurin 21; a "CI result" gate job always reports, so branch protection can require it without blocking docs-only PRs
- Every backend PR builds the image and runs a Trivy gate on fixable CRITICALs; on `main` the report goes to the Security tab and the image is pushed to GHCR (`sha-<short>`, `main`)
- Layered, non-root Dockerfile (moved here from OPS-2.4)
- Actions pinned by commit SHA (trivy-action tags were hijacked earlier this year); Dependabot keeps them current
- Broke / learned: the `/mvnw text eol=lf` rule was anchored to the old repo root, so after the move `mvnw` came out with CRLF and without the executable bit
- The image gate's first run blocked 4 CRITICAL CVEs in `tomcat-embed-core` 11.0.24; Spring Boot 4.1.1 → 4.1.2 brings Tomcat 11.0.27

**Branch:** `chore/OPS-2.3-ci`

---

## 2026-11-21 — DOC-2.2: Docs v2

- `GLOSSARY.md`: one vocabulary for code, tests and docs (Song, Line, Token, Saved word, Sighting, Learner vs Account …)
- Ten ADRs in `docs/adr/`: monorepo, modular monolith + identity + nlp, Compose before k3s, FSRS, R2, own identity service, Traefik, license policy, account vs learner, no external calls at runtime
- `docs/PROJECT_SPEC.md` v2 and `docs/ARCHITECTURE.md` with Mermaid diagrams (context, apps, modules, ingestion, review, deployment)
- DEVLOG moved to `docs/`; README added

**Branch:** `docs/DOC-2.2-docs-v2`

---

## 2026-11-21 — OPS-2.1: Monorepo layout and Spring Modulith backend

- Repo root is now the whole project; the service lives in `backend/`, history kept (`git log --follow` works)
- Package `de.anashabib.content_ingestion` → `de.anashabib.lyricsync`, existing code is the `catalog` module
- Spring Modulith 2.1.3 added (matches Boot 4.1); artifact renamed `lyricsync-backend`; staying on Java 21
- Dropped the unused Spring Data JDBC starter (it was trying to claim the JPA repositories)
- Tests run inside `maven:3.9-eclipse-temurin-21`: the antivirus on the dev PC breaks host Java → Docker connections

**Branch:** `chore/OPS-2.1-monorepo`

---

## 2026-11-21 — BE-1.6: Admin song ingestion endpoint

- `IngestSongUseCase` chains decode → parse → validate, then stores audio, finds or creates the artist, saves the song
- Validation fails before any I/O, so a bad file never writes audio or rows
- `IngestResult` is sealed (Success / ValidationFailure), so the controller's switch covers both outcomes
- `POST /api/content/admin/songs` (multipart): 201 with the song, 422 with line errors
- Upload limit raised to 20 MB (Spring's 1 MB default rejected real MP3s); `app.storage` moved out of the `spring:` block so the context starts
- Known gaps, picked up in Sprint 2: no input validation, no error handler, no auth, not transactional

**Branch:** `feature/BE-1.6-song-ingestion-endpoint`

---

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
