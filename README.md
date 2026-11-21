# lyricsync

Learn German through music. Lyrics light up line by line as the song plays, every word can be tapped for its meaning, base form and gender, and saved words come back in spaced-repetition reviews that replay the exact line you heard them in.

> **Status:** in development. The ingestion backend works; the player, NLP pipeline and accounts are next. See the [roadmap](docs/PROJECT_SPEC.md#12-roadmap).

## Highlights

- **Synced lyrics** from `.lrc` files, with loop-line and slow-down (pitch preserved)
- **Tap any word**: lemma (`ging → gehen`), word type, der/die/das colour coding, separable verbs (`rufe … an → anrufen`)
- **Reviews with the song's own audio**, scheduled with FSRS
- **CEFR levels A1–B2**, guest mode, streaks
- **Runs for about €8.50 a month**: one VPS, everything else on free tiers

## Architecture

A Spring Modulith backend (catalog, dictionary and learning modules), a separate identity service, a Python NLP service with spaCy, and a Next.js web app, connected through RabbitMQ and fronted by Traefik.

```mermaid
flowchart LR
    browser([Browser]) --> traefik[Traefik]
    traefik --> web[web · Next.js]
    traefik --> identity[identity-service]
    traefik --> backend[backend · Spring Modulith]
    backend <--> mq{{RabbitMQ}} <--> nlp[nlp-service · spaCy]
    backend & identity --> pg[(PostgreSQL)]
```

More detail:
- [Architecture overview](docs/ARCHITECTURE.md)
- [Project spec](docs/PROJECT_SPEC.md)
- [Architecture decision records](docs/adr/)
- [Glossary](GLOSSARY.md)
- [Development log](docs/DEVLOG.md)

## Tech stack

| Area | Tools |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Modulith, Spring Data JPA, Flyway, PostgreSQL |
| NLP | Python, FastAPI, spaCy |
| Web | Next.js, TypeScript, Tailwind, shadcn/ui, TanStack Query |
| Messaging | RabbitMQ |
| Testing | JUnit 5, Testcontainers, ArchUnit, pytest, Vitest, Playwright |
| Ops | Docker, Traefik, GitHub Actions, GHCR, Grafana Cloud; k3s + Argo CD planned |

## Running locally

Requirements: Docker, and JDK 21 if you want to run the backend outside a container.

**Whole stack in containers**

```bash
cp .env.example .env                        # optional: change passwords or ports
docker compose --profile app up -d --build  # Postgres, RabbitMQ, Traefik, backend
```

| URL | What |
|---|---|
| http://localhost/swagger-ui.html | API docs (Swagger UI), through Traefik |
| http://localhost/api/… | Backend API, through Traefik |
| http://localhost:8090 | Traefik dashboard |
| http://localhost:15672 | RabbitMQ management (`lyricsync` / `local_dev_only`) |

Health and metrics (`/actuator/health`, `/actuator/prometheus`) are reachable only inside the Docker network, not through Traefik. `docker compose --profile app down` stops everything; add `-v` to wipe the data volumes.

**Backend from your IDE or terminal**

```bash
cd backend
./mvnw spring-boot:run   # starts Postgres and RabbitMQ from ../compose.yaml, then the app on :8080
./mvnw verify            # unit + Testcontainers integration tests
```

## Credits

Dictionary data: [Wiktionary](https://www.wiktionary.org/) via [kaikki.org](https://kaikki.org/) (CC BY-SA).
