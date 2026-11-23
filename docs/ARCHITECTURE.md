# Architecture

How lyricsync is put together. The why behind each choice is in [docs/adr](adr/); the words used here are defined in [GLOSSARY.md](../GLOSSARY.md).

## 1. System context

```mermaid
flowchart LR
    learner([Learner / Guest])
    admin([Admin])
    subgraph lyricsync
        app[lyricsync platform]
    end
    r2[(Cloudflare R2 + CDN<br/>audio)]
    deepl[DeepL API Free<br/>line translations]
    kaikki[kaikki.org<br/>Wiktionary extract]
    resend[Resend<br/>emails]
    grafana[Grafana Cloud<br/>metrics + logs]

    learner -->|listens, taps, reviews| app
    admin -->|uploads, reviews, publishes| app
    learner -.->|streams audio, cached by browser + CDN| r2
    app -->|stores audio| r2
    app -->|once per line, at ingestion| deepl
    app -->|one-off import| kaikki
    app -->|verification + reset mails| resend
    app -->|telemetry| grafana
```

Learners never cause a call to DeepL or kaikki.org; those run only during ingestion or import ([ADR-0010](adr/0010-no-external-calls-at-runtime.md)).

## 2. Apps

```mermaid
flowchart TB
    browser([Browser])
    edge[Cloudflare edge<br/>one origin]
    traefik[Traefik<br/>TLS, routing, rate limits]
    web[web<br/>Next.js on Workers]
    backend[backend<br/>Spring Boot + Modulith]
    identity[identity-service<br/>Spring Boot]
    nlp[nlp-service<br/>FastAPI + spaCy]
    pg[(PostgreSQL<br/>schema per module)]
    mq{{RabbitMQ}}

    browser --> edge
    edge -->|/*| web
    edge -->|/api/*| traefik
    traefik -->|/api/auth/*| identity
    traefik -->|/api/*| backend
    backend -->|verifies JWT via JWKS| identity
    backend <--> pg
    identity <--> pg
    backend <-->|song.ingested / song.analyzed| mq
    nlp <--> mq
    identity -->|account.created / account.deleted| mq
```

| App | Owns | Talks to |
|---|---|---|
| `web` | All UI, guest data in the browser; runs on Cloudflare Workers ([ADR-0012](adr/0012-web-on-cloudflare-workers.md)) | backend and identity through the same origin (`/api/*`) |
| `backend` | catalog, dictionary and learning modules | Postgres, RabbitMQ, R2, DeepL |
| `identity-service` | Accounts, passwords, tokens, verification mails | Postgres, RabbitMQ, Resend |
| `nlp-service` | Tokens, lemmas, morphology, separable verbs | RabbitMQ only (stateless) |

## 3. Inside the backend

```mermaid
flowchart LR
    subgraph backend [backend · one JVM]
        direction LR
        catalog[catalog<br/>songs, lines, tokens,<br/>ingestion, publishing]
        dictionary[dictionary<br/>entries, forms]
        learning[learning<br/>learner, saved words,<br/>sightings, reviews, streaks]
    end
    catalog -->|links tokens via API| dictionary
    learning -->|reads lines via API| catalog
    learning -->|reads entries via API| dictionary
    catalog -. SongCompleted / SongPublished events .-> learning
```

Every module has the same internal shape:

```
<module>/
├── domain/        pure Java: records, rules, no Spring or JPA
├── application/   use cases + ports (in: what the module offers, out: what it needs)
├── adapter/
│   ├── in/        web controllers, message listeners
│   └── out/       JPA, R2, DeepL, RabbitMQ
└── config/        wiring
```

Rules ([ADR-0002](adr/0002-modular-monolith.md)):
- A module owns its Postgres schema; no foreign keys cross schemas.
- Only a module's top-level package is its public API; sub-packages are internal.
- Queries go through public APIs, state changes travel as events. Events between modules go through the Modulith event publication registry, a transactional outbox, so they survive crashes.
- Two tests fail the build when a rule is broken: `ModularityTest` (`ApplicationModules.verify()`, module boundaries) and `HexagonalArchitectureTest` (ArchUnit, layers inside each module). The domain may depend only on the JDK and its own domain packages.
- `./mvnw verify` also writes module diagrams and canvases (PlantUML, AsciiDoc) to `backend/target/spring-modulith-docs`.

## 4. Ingestion

```mermaid
sequenceDiagram
    actor Admin
    participant C as catalog
    participant R2 as R2
    participant MQ as RabbitMQ
    participant N as nlp-service
    participant D as dictionary
    participant DL as DeepL

    Admin->>C: POST /api/admin/songs (audio, .lrc, metadata)
    C->>C: decode → parse → validate
    alt invalid
        C-->>Admin: 422 with every problem
    else valid
        C->>R2: store audio, named by its SHA-256
        C->>C: one transaction: artist + song + lines (PROCESSING)
        Note over C,R2: if the transaction fails, the audio this upload wrote is deleted again
        C-->>Admin: 201 Created + Location
        C->>MQ: SongIngested (via outbox)
        MQ->>N: song.ingested
        N->>N: tokenise, lemma, POS, morphology, svp pairs
        N->>MQ: song.analyzed
        MQ->>C: tokens
        C->>D: link tokens (forms → lemma + POS)
        C->>DL: translate lines (once)
        C->>C: READY_FOR_REVIEW or FAILED
    end
```

Audio goes to storage before the database transaction, so no database locks are held during slow I/O. If the process dies between the two steps, an unreferenced file is left behind; that's harmless (nothing points at it), and a later clean-up job can sweep such files. Re-delivering a message replaces the song's tokens, so the pipeline is safe to retry. Messages that keep failing land in a dead-letter queue, and the song shows FAILED with the reason.

## 5. Review

```mermaid
sequenceDiagram
    actor L as Learner
    participant W as web
    participant B as backend (learning)
    participant CA as catalog API
    participant R2 as R2

    L->>W: open /review
    W->>B: GET /api/reviews/due
    B->>CA: line text + clip times for each chosen sighting
    B-->>W: due saved words with clip info + audioUrl
    W->>R2: range request, play start_ms → end_ms (browser cache first)
    L->>W: Good
    W->>B: POST /api/reviews/{id}/grade
    B->>B: FSRS → next due time, log review, update streak
```

## 6. Caching

Rule of thumb: cache what is shared and rarely changes as close to the learner as possible; never cache one learner's data on the server ([ADR-0011](adr/0011-caching-strategy.md)).

```mermaid
flowchart LR
    tq[TanStack Query<br/>memory] --> bc[Browser HTTP cache]
    bc --> cdn[Cloudflare CDN]
    cdn --> traefik[Traefik]
    traefik --> caffeine[Caffeine<br/>in the backend]
    caffeine --> pg[(PostgreSQL)]
    cdn --> r2[(R2 audio)]
```

| Request | First hit that answers it |
|---|---|
| Back to a song in the same session | TanStack Query (no request) |
| The song's MP3 again, any time | Browser cache (immutable, content-hashed URL) |
| Song JSON after a reload | Browser revalidates with `ETag` and gets `304 Not Modified` |
| Song JSON for another learner | Cloudflare CDN |
| First request after a publish | Caffeine, then PostgreSQL once |
| Due reviews, saved words, streak | Always the backend and database (`private, no-store`) |

`SongPublished`, `SongReanalysed` and `SongUnpublished` events clear the affected Caffeine entries and bump the song's version, which changes its `ETag`.

## 7. Authentication

- identity-service issues a short-lived RS256 access token and a rotating refresh token, both as httpOnly, `SameSite=Lax` cookies on the app's own domain ([ADR-0006](adr/0006-own-identity-service.md)).
- The backend validates tokens against identity's JWKS endpoint, checks the `ADMIN` role for admin routes, and never sees passwords.
- `AccountCreated` creates the learner; `AccountDeleted` removes all learner data ([ADR-0009](adr/0009-account-and-learner.md)).

## 8. Deployment

**v1: Docker Compose on one VPS** ([ADR-0003](adr/0003-compose-first-kubernetes-later.md))

```mermaid
flowchart LR
    gh[GitHub Actions] -->|build, test, push| ghcr[(GHCR)]
    gh -->|ssh: compose pull + up| vps
    subgraph vps [Hetzner CX33 · Docker Compose]
        traefik[Traefik + Let's Encrypt] --> backend & identity
        backend & identity --> pg[(Postgres)]
        backend & identity & nlp --> mq{{RabbitMQ}}
        alloy[Grafana Alloy]
        backup[nightly pg_dump]
    end
    ghcr --> vps
    alloy --> grafana[Grafana Cloud]
    gh -->|vinext-cloudflare deploy| workers[Cloudflare Workers · web]
    backup --> r2[(R2)]
```

**Stretch: single-node k3s.** Same images; Argo CD syncs Kustomize manifests from the repo, CloudNativePG runs Postgres with backups to R2, cert-manager handles certificates, and Sealed Secrets keeps secrets in git.

## 9. Repository layout

```
/
├── backend/            Spring Boot + Modulith
├── identity-service/   (Sprint 5)
├── nlp-service/        (Sprint 4)
├── web/                (Sprint 3)
├── docs/               spec, architecture, ADRs, devlog
├── compose.yaml        local stack (OPS-2.4)
└── GLOSSARY.md
```
