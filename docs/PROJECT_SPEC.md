# lyricsync — Project Spec (v2)

Learn German through music. Working name in code: **lyricsync**; product name and domain to be decided.

Terms in *italics* are defined in [GLOSSARY.md](../GLOSSARY.md). Decisions behind this spec are recorded in [docs/adr](adr/).

---

## 1. What this is

A web platform where **English speakers learn German by listening to songs**.

- *Lines* highlight in time with the audio (from `.lrc` timestamps).
- Tapping a word opens its meaning, *lemma*, word type and *gender*.
- *Saved words* come back in spaced-repetition *reviews* that replay the song line they were saved from.
- Songs are grouped by *level*: A1, A2, B1, B2.

### Goals, in priority order
1. **Portfolio piece for the German job market**, built to production standards: clean architecture, tests, CI, containers, docs.
2. **A real path to a product.** Free for learners for the first 3 months after launch.
3. **Running cost close to €0**: only the VPS and the domain cost money (section 11).

### Language pair
English (UI and translations) → German. The data model leaves room for more pairs; nothing multi-language is built now.

---

## 2. Status (2026-11-21)

- **Sprint 1 done:** `.lrc` parser, encoding decoder, validator, Flyway baseline, JPA persistence, admin ingestion endpoint.
- **Sprint 2 in progress:** monorepo + Spring Modulith (done), these docs, CI, local stack, architecture tests, parser fixes, song lifecycle, ingestion hardening.
- `.lrc` files with exact per-line timestamps already exist for the first songs.

---

## 3. MVP features

| # | Feature | Summary |
|---|---------|---------|
| F1 | **Synced lyrics player** | The current *line* highlights and auto-scrolls with the audio. |
| F2 | **Tap a word → word card** | *Lemma* (`ging → gehen`), word type, article/*gender*, 1–3 English meanings. Tapping pauses the audio. |
| F3 | **der/die/das colours** | Nouns coloured by *gender*, each colour paired with a small `m`/`f`/`n` marker so colour is never the only signal. |
| F4 | **Loop line + slow speed** | Loop the current line; speeds 1× / 0.85× / 0.75× with pitch preserved. |
| F5 | **Separable verbs** | In "Ich **rufe** dich **an**", either part highlights its *partner token* and shows **anrufen**. |
| F6 | **Save word** | Saves the *dictionary entry* with a *sighting* (song, line, token). Saving the same word from another song adds a sighting. |
| F7 | **Review with song audio** | FSRS-scheduled *reviews*; each one replays the line of one of the word's sightings, rotating between them. |
| F8 | **Line translation on request** | Per line, or for the whole song. |
| F9 | **Levels** | Library tabs A1–B2; the learner picks a level at onboarding. |
| F10 | **Admin ingestion + review** | Upload audio + `.lrc`, preview the sync, the pipeline analyses the song, the admin fixes *unmatched* tokens and edits translations, then publishes. |
| F11 | **Guest mode** | Play songs and tap words without an account. Sign-up is asked for only when saving a word. |
| F12 | **Streak + daily goal** | Daily review goal (default 10) and a streak counter (section 5.3). |
| F13 | **Privacy controls** | Self-service account deletion and JSON data export; Impressum and Datenschutzerklärung pages. |

### Out of scope for the MVP
- Word-level alignment (we sync *lines*, not words).
- Google login (first item after launch).
- AI line explanations, gap-fill exercises, "you know X% of this song", automatic level suggestion.
- Payments, native apps, social features, other language pairs.

---

## 4. Levels

- `A1`, `A2`, `B1`, `B2`. The admin sets a song's level; it is required for *publishing*.
- The learner picks a level at onboarding ("I'm a beginner (A1)" … "Upper intermediate (B2)") and can change it in settings. Library tabs can be switched freely.
- A song counts as a *completion* once the learner has played it to the end. Each tab shows progress, e.g. `4 / 12 songs completed`.
- After launch: suggest a level from vocabulary difficulty using an openly licensed frequency list; the admin can override.

---

## 5. User flows

### 5.1 First visit (guest) and sign-up
```
Landing → "Start listening" → Choose your level (stored in the browser)
→ Library on that level tab → play songs, tap words (no account needed)
→ "Save to review" → Sign up (email + password) → email verification mail sent
→ guest level + completions are merged into the new learner → the pending save completes
```

### 5.2 Learn with a song (core loop)
```
Library → song card → Song page
  → Play: lines highlight in sync, view auto-scrolls
  → Tap a word → audio pauses → word card
       ├─ "Save to review"  → saved (toast "Saved: gehen", or "Saved — added this line")
       ├─ "Replay line"     → plays the current line once
       └─ close             → audio resumes (unless "stay paused" is on)
  → Loop line / change speed / toggle translation
  → Song ends → completion recorded → "You saved 6 words. Review now?"
```

### 5.3 Review
```
Nav badge "12 due" → /review
  → Front: word with article ("das Herz") + the sighting's line audio plays
  → "Show" → back: meanings + the line, word highlighted
  → Grade: Again / Hard / Good / Easy → FSRS sets the next due time
  → Session summary + streak
```
A day counts toward the *streak* (learner's timezone) when the daily goal is met, or when everything due was reviewed and at least one song was completed. No streak freezes in the MVP.

### 5.4 Admin: add a song
```
/admin/songs/new
  → audio (MP3 ≤ 20 MB) + .lrc file or pasted text + title, artist, level, license + license note
  → sync preview: play the audio with lines highlighting; duration read from the file
  → Submit → PROCESSING   (DRAFT until the analysis pipeline exists, BE-4.4)
  → pipeline (section 7.4): parse → NLP → dictionary linking → line translation
  → READY_FOR_REVIEW (or FAILED with the reason)
  → /admin/songs/{id}/tokens: fix unmatched tokens, edit line translations
  → Publish → PUBLISHED (visible in the library)
```

---

## 6. Frontend (`web/`)

### 6.1 Stack
Next.js (App Router) + TypeScript + Tailwind, shadcn/ui, Vaul (mobile bottom sheet), TanStack Query. Vitest + Testing Library; Playwright for one or two end-to-end happy paths. **Mobile first.** Served from the VPS as a standalone Node server.

### 6.2 Routes
| Route | Purpose |
|-------|---------|
| `/` | Landing |
| `/signup`, `/login`, `/verify-email`, `/reset-password` | Auth |
| `/onboarding/level` | Pick a level (guest or learner) |
| `/songs` | Library with level tabs (`?level=A1`) |
| `/songs/[slug]` | Player + lyrics |
| `/review` | Review session |
| `/vocabulary` | Saved words (search, delete) |
| `/settings` | Level, daily goal, timezone, playback defaults, gender colours, export, delete account |
| `/impressum`, `/datenschutz` | Legal pages |
| `/admin/songs`, `/admin/songs/new`, `/admin/songs/[id]/tokens` | Admin |

### 6.3 Key components
- `LyricsPlayer`: owns the single `<audio>` element; play/pause, seek, loop, speed.
- `LyricLine`: active / past / upcoming states, optional translation row.
- `WordToken`: a `<button>` per token (touch, mouse, keyboard); gender colour + marker; highlights its partner.
- `WordCard`: Popover at ≥ 768 px, Vaul bottom sheet below.
- `PlayerControls`, `LevelTabs`, `SongCard`, `ReviewCard`, `ReviewRatingBar`, `StreakBadge`.

### 6.4 Player behaviour
- **Sync:** read `audio.currentTime` in `requestAnimationFrame` and find the active line by binary search over start times.
- **Line end:** the next line's start; the last line ends at the song's duration (computed at ingestion).
- **Loop:** when `currentTime >= activeLine.end`, seek to `activeLine.start`.
- **Speed:** `playbackRate` with `preservesPitch = true`.
- **Review clips:** seek to the line start, play, stop at the line end (checked in rAF).
- **Seeking needs HTTP range requests**: R2 supports them; the dev storage adapter must too.
- **No request on tap:** the song response embeds tokens and meanings.
- **Coming back to a song costs nothing:** TanStack Query keeps published songs in memory, and the MP3 comes from the browser cache because its URL never changes (section 7.7).

### 6.5 Gender colours
Three colours that pass WCAG AA on the background, each with an `m`/`f`/`n` marker. Only nouns are coloured; plurals use their singular's gender. Legend in the player header; can be turned off in settings.

### 6.6 Guest data
Level, completions and a pending save live in `localStorage`. On sign-up the web app sends them once to the backend to merge, then clears them. Guests have no server-side data.

---

## 7. Backend

### 7.1 Apps
| App | Tech | Owns |
|-----|------|------|
| `backend` | Spring Boot 4, Spring Modulith, Java 21 | modules **catalog**, **dictionary**, **learning** |
| `identity-service` | Spring Boot 4, Java 21 | accounts, passwords, email verification, password reset, tokens |
| `nlp-service` | Python, FastAPI, spaCy `de_core_news_md` | tokenising, lemma, part of speech, morphology, separable verbs |

Infrastructure: PostgreSQL (one schema per module/service), RabbitMQ, Cloudflare R2, Traefik. Docker Compose for local and v1 production.

### 7.2 Module rules (backend)
- Each module is hexagonal inside: domain (no framework imports) → application (use cases, ports) → adapters.
- Each module owns its schema; no foreign keys across schemas.
- Modules use each other only through a module's public API (queries) or events (state changes). `ApplicationModules.verify()` and ArchUnit tests enforce this.
- Learning reads word and line data live through the dictionary and catalog APIs.

### 7.3 Word lookup at €0
1. **Dictionary import** (one-off job): the German extract from kaikki.org (English Wiktionary data) becomes *dictionary entries* (lemma, part of speech, gender, meanings) and *forms* (inflected spelling → entry).
2. **NLP at ingestion:** nlp-service returns each token's text, character offsets, lemma, part of speech and morphology. For separable verbs it pairs the prefix (`svp` dependency) with its verb, so `rufe … an` gives `anrufen`.
3. **Linking:** first look the surface up in *forms*, then fall back to (lemma, part of speech). Prefer entries with the same part of speech. Gender comes from the entry, with spaCy morphology as a fallback.
4. **Unmatched tokens** (names, slang): marked Unmatched for the admin.
5. **Line translations:** each line goes to DeepL API Free once at ingestion; admins can edit the result.
6. Learners never trigger an external call.

### 7.4 Ingestion pipeline
```
POST /api/admin/songs (multipart)
  → catalog: validate, store audio in R2 under its SHA-256, parse .lrc, save song + lines (PROCESSING)
  → event SongIngested ──(outbox → RabbitMQ song.ingested)──► nlp-service
  → nlp-service analyses lines → song.analyzed (tokens)
  → catalog saves tokens, dictionary links them, lines are translated
  → READY_FOR_REVIEW
```
- Idempotent: re-running a song replaces its tokens.
- Failures go to a dead-letter queue; the song becomes FAILED with the reason shown to the admin.

### 7.5 API (behind Traefik)
| Method & path | App | Notes |
|---|---|---|
| `POST /api/auth/signup`, `/login`, `/refresh`, `/logout` | identity | Cookies: short access token, rotating refresh token |
| `POST /api/auth/verify-email`, `/password/forgot`, `/password/reset` | identity | Mails via Resend |
| `DELETE /api/auth/account` | identity | Emits `AccountDeleted` |
| `GET /api/songs?level=A1&page=` | backend | Published songs only; cacheable (section 7.7) |
| `GET /api/songs/{slug}` | backend | Song + lines + tokens with meanings embedded, plus `audioUrl` (public CDN URL); `ETag` from the song version |
| `GET`, `PATCH /api/me/learner` | backend | Level, daily goal, timezone, settings |
| `POST /api/me/guest-import` | backend | Merge guest data once after sign-up |
| `GET /api/me/export` | backend | GDPR JSON export |
| `POST /api/vocab` | backend | `{ tokenId }`: server derives entry, song and line; adds a sighting if already saved |
| `GET /api/vocab`, `DELETE /api/vocab/{id}` | backend | |
| `GET /api/reviews/due?limit=20` | backend | With the sighting's clip times |
| `POST /api/reviews/{savedWordId}/grade` | backend | `{ grade: AGAIN \| HARD \| GOOD \| EASY }` |
| `POST /api/progress/songs/{id}/complete` | backend | |
| `GET /api/me/streak` | backend | |
| `POST /api/admin/songs`, `GET /api/admin/songs` | backend | ADMIN |
| `GET /api/admin/songs/{id}/tokens?status=UNMATCHED` | backend | ADMIN |
| `PATCH /api/admin/tokens/{id}`, `PATCH /api/admin/lines/{id}` | backend | Fix entry / edit translation |
| `POST /api/admin/songs/{id}/publish` | backend | Needs a level and no unmatched tokens |

Example token in `GET /api/songs/{slug}`:
```json
{
  "id": 812, "index": 2, "text": "ging", "lemma": "gehen",
  "pos": "VERB", "gender": null,
  "separableLemma": null, "partnerTokenId": null,
  "meanings": ["to go", "to walk"]
}
```

### 7.6 Events
| Event | From → to | Carries |
|---|---|---|
| `SongIngested` | catalog → nlp-service (RabbitMQ) | song id, lines |
| `song.analyzed` | nlp-service → catalog (RabbitMQ) | tokens per line |
| `AccountCreated` | identity → learning (RabbitMQ) | account id, email verified flag |
| `AccountDeleted` | identity → learning, catalog (RabbitMQ) | account id |
| `SongPublished`, `SongReanalysed`, `SongUnpublished`, `SongCompleted` | inside the backend (Modulith) | ids; the first three also clear cached song responses |

### 7.7 Caching
Details and reasons: [ADR-0011](adr/0011-caching-strategy.md).

| What | Where it's cached | Until |
|---|---|---|
| Audio | Browser + Cloudflare CDN: public URL `audio/<sha256>.mp3`, `Cache-Control: public, max-age=31536000, immutable` | Forever (a new upload is a new URL) |
| Published song JSON, library pages | Browser (TanStack Query, `ETag` revalidation), CDN (`max-age=60, stale-while-revalidate=600`), backend Caffeine | The song is republished, re-analysed or unpublished |
| Frequent dictionary forms (ingestion) | Backend Caffeine | Size limit |
| Learner data (reviews, saved words, streak, settings) | Browser memory only; server sends `Cache-Control: private, no-store` | Every change the learner makes |

No Redis while there's a single backend instance; no Hibernate second-level cache. Cache hit ratios are exported as Prometheus metrics.

---

## 8. Data model

**catalog**
- `language` (code, name)
- `artist` (id, name — unique, case-insensitive)
- `song` (id, slug — unique, title, artist_id, language_code, cefr_level, duration_ms, status, version — bumped on every update, failure_reason — only when FAILED, license_type, license_note, audio_key (`audio/<sha256>.mp3`), cover_image_url, sync_granularity, created_at, published_at — only when PUBLISHED)
- `song_line` (id, song_id, line_index, raw_text, start_ms, end_ms > start_ms, is_instrumental, contextual_translation, cultural_note)
- Every rule the domain enforces is also a database constraint, so a bug or a manual SQL fix can't store a song the code would refuse.
- `token` (id, line_id, token_index, surface, lemma, pos, gender, morph, char_start, char_end, separable_lemma, partner_token_id, dictionary_entry_id, match_status)

**dictionary**
- `dictionary_entry` (id, lemma, pos, gender, meanings, source = wiktionary | manual | deepl)
- `dictionary_form` (form, entry_id, tags)

**learning**
- `learner` (account_id, cefr_level, daily_goal, timezone, settings, created_at)
- `saved_word` (id, learner_id, dictionary_entry_id, created_at) — unique (learner_id, dictionary_entry_id)
- `sighting` (id, saved_word_id, song_id, line_id, token_id, created_at) — unique (saved_word_id, line_id)
- `review_state` (saved_word_id, due_at, stability, difficulty, state, reps, lapses, last_reviewed_at)
- `review_log` (id, saved_word_id, grade, reviewed_at, elapsed_days, scheduled_days)
- `song_completion` (learner_id, song_id, completed_at)
- `streak_day` (learner_id, day, reviews_done, goal_met)

**identity**
- `account` (id, email, password_hash, role, email_verified_at, created_at)
- `refresh_token` (id, account_id, token_hash, family_id, expires_at, revoked_at)
- `email_token` (id, account_id, purpose = verify | reset, token_hash, expires_at, used_at)

---

## 9. Quality bar
- **Architecture:** hexagonal modules; framework-free domain; Modulith + ArchUnit boundary tests.
- **Tests:** JUnit 5 + Testcontainers (Postgres, RabbitMQ); pytest; Vitest + Playwright.
- **CI:** GitHub Actions builds and tests every PR; images for `main` go to GHCR with a vulnerability scan.
- **API docs:** OpenAPI (springdoc) per Spring app.
- **Observability:** Actuator health, Prometheus metrics and JSON logs, shipped to Grafana Cloud's free tier.
- **Performance:** layered caching from the browser to the backend (section 7.7); returning to a song downloads nothing.
- **Accessibility:** keyboard-navigable player and tokens, visible focus, WCAG AA contrast, never colour alone.
- **Security:** BCrypt, httpOnly cookies, ADMIN checked in the service (and routed only via Traefik), input validation, upload size and type limits, rate limits at the edge.
- **Privacy:** only essential cookies, no tracking, account deletion and export.
- **README:** architecture diagram, one-command local run, live demo link, demo GIF.

---

## 10. Content and licensing
- Publish only songs whose license allows translation and commercial use: public domain or CC0, CC BY, CC BY-SA, own recordings, or written permission. NonCommercial and NoDerivatives are rejected.
- Every song stores its license and a note (source URL, attribution, permission).
- Seed content: German folk songs with public-domain lyrics and openly licensed recordings (for example from Wikimedia Commons), at least 3 per level.
- Footer credit: "Dictionary data: Wiktionary (CC BY-SA)".

---

## 11. Cost plan (first 3 months)

| Item | Choice | Cost |
|------|--------|------|
| Word meanings | Wiktionary data (kaikki.org) in Postgres | €0 |
| Line translations | DeepL API Free (500k characters/month), once per line | €0 |
| NLP | spaCy, self-hosted | €0 |
| Audio | Cloudflare R2 free tier (10 GB, no egress fees) | €0 |
| CDN | Cloudflare (comes with the domain) caches audio and public API responses | €0 |
| Email | Resend free tier | €0 |
| Monitoring | Grafana Cloud free tier | €0 |
| CI + images | GitHub Actions + GHCR (public repo) | €0 |
| Server | Hetzner CX33 (4 vCPU, 8 GB) | ~€8.50/month |
| Domain | .com / .dev via Cloudflare | ~€10–15/year |

The web app is self-hosted on the VPS (Vercel's hobby tier does not allow commercial use).

---

## 12. Roadmap

| Sprint | Goal |
|--------|------|
| **1 · LRC ingestion core** (done) | Parser, decoder, validator, schema, persistence, admin endpoint |
| **2 · Foundation** | Monorepo + Modulith, docs v2, CI, local stack, architecture tests, parser fixes, song lifecycle, ingestion hardening |
| **3 · Player v1** | R2 audio behind the CDN, cached public song API, Next.js app, synced player, loop and speed |
| **4 · NLP and dictionary** | nlp-service, dictionary import, RabbitMQ pipeline, token linking, DeepL, word card, gender colours |
| **5 · Accounts and review** | identity-service, learner profile, saved words + sightings, FSRS reviews, guest merge, streak |
| **6 · Levels, admin and launch** | Onboarding, library progress, admin UI with sync preview, token review, publishing, privacy features, VPS + Compose, CD, monitoring, backups, seed songs, README — **launch** |
| **7 · Kubernetes (stretch)** | k3s, Argo CD, CloudNativePG, migration from Compose |

After launch: Google login, AI line explanations (generated once at ingestion), gap-fill mode, vocabulary coverage per song, level suggestion.

---

## 13. Open decisions
1. Product name and domain.
2. Which openly licensed songs to seed for each level.
