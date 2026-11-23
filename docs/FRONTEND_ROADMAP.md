# Frontend Roadmap

Every frontend ticket from Sprint 3 to launch, in build order. Each ticket follows [DESIGN.md](../DESIGN.md) (tokens, motion, components) and [PRODUCT.md](../PRODUCT.md) (users, voice, onboarding). Backend tickets are listed only where a screen depends on them.

The app is Next.js on **vinext**, deployed to **Cloudflare Workers** (ADR-0012), in `web/`. Stack: TypeScript, Tailwind, shadcn/ui, TanStack Query, Motion, Lucide icons, self-hosted fonts.

**Definition of done for every ticket**
- Built only from design tokens; no raw colours, sizes or timings.
- Checked at 390 px and 1440 px, keyboard only, and with `prefers-reduced-motion`.
- Text contrast at least 4.5:1; gender colours always with their `m`/`f`/`n` marker.
- Loading uses skeletons, empty states have one line and one action, errors offer "Try again".
- Unit tests (Vitest) for logic; Playwright for the happy paths named below.
- Server rendering stays light: data that changes per learner is fetched in the browser, so each Worker request stays under the free plan's 10 ms CPU.

---

## Sprint 3 · Player v1

| Ticket | What | Design focus | Depends on |
|---|---|---|---|
| **FE-3.3** Next.js on Workers scaffold | `create-vinext-app` in `web/`, `cloudflare.config.ts`, Tailwind with the DESIGN.md tokens (colours, type scale, radii, spacing), self-hosted fonts, motion tokens, shadcn/ui, TanStack Query, Vitest, Playwright, ESLint, CI job, deploy to `*.workers.dev`. **App shell:** sidebar (desktop), collapsed sidebar (tablet), bottom tab bar (phone), page transitions. ADR-0012. | `night` first paint, the token layer, the No-Bounce and Reduced-Motion rules built into the motion helpers | Workers API token (owner) |
| **FE-3.4** Synced lyrics player | `LyricsPlayer` and `LyricLine`: `requestAnimationFrame` sync, binary search for the current line, the **singing line** (size and colour change, pink underline filling over the line's duration), smooth auto-scroll with "Back to the song", translation lines. Audio `preload="metadata"` from R2. | Signature motion 4, the One Voice rule | BE-3.1, BE-3.2 |
| **FE-3.5** Player controls | Player bar and mobile mini player with expanding sheet: **morphing play button** with breathing glow, **wave timeline** (sine while playing, straight when paused, seek by click/drag/keys), times in JetBrains Mono, loop line, speed 1 / 0.85 / 0.75 with pitch preserved, translation toggle, space bar. | Signature motions 1–3, blur only on the bars | FE-3.4 |
| **FE-3.6** Songs list and first Playwright test | Song card (cover, title, level badge, no artist), card grid, Library page with level tabs, the **song page header** (280 px cover, title, artist, level; cover shrink on phones), **ambient glow** from the cover, generated artwork for songs without a cover. Playwright: open the list → open a seeded song → the active line changes while playing. | Card hover lift, stagger, glow crossfade | FE-3.5 |
| **FE-3.7** Mini player timeline and play from a line | The phone **mini player** grows to 76 px with the wave timeline along its bottom edge (tap or drag to seek, 44 px touch area). **Playing from a line:** jumps to its start and plays, the loop follows, auto-scroll resumes. Phone: tap anywhere on a line; PC: ▶ in the left column (hover, focus, active line), the translation, the line's empty space; a "Play from: …" button per line for keyboards and screen readers. Scrolling never changes the time. | The Lyric line table, ▶ never pink | FE-3.6 |

*Proposed:* move the guest part of **onboarding** (FE-6.1, below) to the end of Sprint 3, so the first public demo already greets the visitor and shows their level's songs. It needs only the browser, no backend.

## Sprint 4 · NLP and dictionary

| Ticket | What | Design focus | Depends on |
|---|---|---|---|
| **FE-4.8** Word tokens and word card | Every word a button; nouns in their gender colour with the `m`/`f`/`n` marker; the legend in the song header; separable-verb partners highlight together; the **word card** (popover on desktop, bottom sheet on phones) with lemma, article, word type, meanings, Replay line (plays the line once); opening pauses the audio. **Phone:** press and hold a word (500 ms, plum highlight, no copy menu) to open it, while a tap still plays from the line; the **press-and-hold hint** toast (7 s, until dismissed or used). **PC:** click a word. Tapping a word with translations off shows that line's translation. The **guest lookup limit** (5 a day, counted in the browser) and its card. | The Marker rule, word-card pop with `spring-soft`, the hint toast | BE-4.7 |

## Sprint 5 · Accounts and review

| Ticket | What | Design focus | Depends on |
|---|---|---|---|
| **FE-5.6** Auth and guest merge | Sign up (prefilled with the onboarding name), log in, verify email, reset password; guest data (name, level, reasons, daily goal, completions, a pending save, hints seen) sent once on sign-up, then cleared; the pending save completes after sign-up or log-in. **Save to review** in the word card; the **sign-up prompt** for guests (Sign up / I already have an account) from Save and from the lookup limit; learners have no lookup limit. Account block switches from guest to learner. | Calm forms, inline errors with icon and sentence | ID-5.1, ID-5.2, BE-5.3, BE-5.4 |
| **FE-5.7** Review and vocabulary | Review: card front with the word and its article, the line's audio clip, **Show**, grade bar (Again, Hard, Good, Easy) with intervals in mono, summary *"Nice work, Anas"*. Vocabulary: saved words list, search, sort, empty state. | One card at a time, gentle card transitions | BE-5.5 |
| **FE-5.8** Streak and daily goal | Streak flame and number, daily-goal ring in the account block, Home's **Reviews due** card. | Steady Numbers rule | BE-5.8 |

## Sprint 6 · Levels, admin and launch

| Ticket | What | Design focus | Depends on |
|---|---|---|---|
| **FE-6.1** Onboarding and Home | The four onboarding screens (name, level, why German, daily time) with option cards and sideways step motion; *"Your A2 songs are ready, Anas"*; **Home** in full: welcome line, continue listening, reviews due, songs for your level, recently added; completion tracking ("4 / 12 completed" per level); Settings for all onboarding answers. | Option-card selection, staggered Home sections | BE-5.3 (learner fields) |
| **FE-6.2** Admin songs | Song list with statuses and failure reasons, upload form (audio, `.lrc` or pasted text, title, artist, level, license and note, **cover and cover note**), sync preview with the real player, duration read from the audio file. Work-tool style. | Dense tables, no glow | BE cover upload, BE-2.8 |
| **FE-6.3** Token review | Fix unmatched words, edit line translations, publish (needs a level and no unmatched words). | Work-tool style | BE-6.3 |
| **FE-6.4** Privacy pages | Impressum, Datenschutz (naming Cloudflare, R2, Resend, Grafana as processors), credits footer (Wiktionary CC BY-SA), data export and account deletion in Settings. | Read mode, 65ch | BE/ID-6.4 |
| **FE-6.5** Landing page | The public front door in **Persuade** mode: a full-bleed cover glow, the live wave as the hero, one line of promise, "Start listening" into onboarding, three short sections. No invented testimonials. | Bolder within the same tokens | FE-3.x, FE-6.1 |

## After launch

- Google sign-in button on the auth pages (ID-B.1).
- "Explain this line" panel (AI, generated once at ingestion).
- Gap-fill mode and "You know X % of this song".
- Light theme (only if learners ask for it).
