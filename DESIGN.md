---
name: lyricsync
description: Learn German through music. Lyrics light up in time with the song; every word explains itself.
colors:
  night: "#12081F"
  dusk: "#1E1030"
  plum: "#411467"
  violet: "#8A37D2"
  violet-light: "#B98AF0"
  karaoke: "#EE1B6C"
  amber: "#FF871F"
  teal: "#32989A"
  teal-light: "#4FC1C3"
  ink: "#F5F3F7"
  ink-muted: "#B9AEC6"
  gender-der: "#6EA8FE"
  gender-die: "#FF7A8A"
  gender-das: "#8BD66B"
typography:
  display:
    fontFamily: "Plus Jakarta Sans, system-ui, sans-serif"
    fontSize: "clamp(2.25rem, 1.5rem + 3vw, 3.5rem)"
    fontWeight: 700
    lineHeight: 1.1
    letterSpacing: "-0.02em"
  headline:
    fontFamily: "Plus Jakarta Sans, system-ui, sans-serif"
    fontSize: "1.75rem"
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: "-0.01em"
  title:
    fontFamily: "Plus Jakarta Sans, system-ui, sans-serif"
    fontSize: "1.125rem"
    fontWeight: 600
    lineHeight: 1.3
  lyric-active:
    fontFamily: "Plus Jakarta Sans, system-ui, sans-serif"
    fontSize: "clamp(1.75rem, 1.2rem + 2vw, 2.5rem)"
    fontWeight: 600
    lineHeight: 1.25
  lyric:
    fontFamily: "Plus Jakarta Sans, system-ui, sans-serif"
    fontSize: "1.25rem"
    fontWeight: 500
    lineHeight: 1.4
  body:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.5
  translation:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: "0.9375rem"
    fontWeight: 400
    lineHeight: 1.45
  label:
    fontFamily: "Inter, system-ui, sans-serif"
    fontSize: "0.8125rem"
    fontWeight: 500
    lineHeight: 1.3
    letterSpacing: "0.01em"
  mono:
    fontFamily: "JetBrains Mono, ui-monospace, monospace"
    fontSize: "0.8125rem"
    fontWeight: 500
    lineHeight: 1.3
rounded:
  sm: "8px"
  md: "12px"
  lg: "16px"
  xl: "24px"
  pill: "9999px"
spacing:
  1: "4px"
  2: "8px"
  3: "12px"
  4: "16px"
  6: "24px"
  8: "32px"
  12: "48px"
  16: "64px"
components:
  button-primary:
    backgroundColor: "{colors.violet}"
    textColor: "{colors.ink}"
    typography: "{typography.label}"
    rounded: "{rounded.pill}"
    padding: "12px 24px"
    height: "44px"
  button-secondary:
    backgroundColor: "transparent"
    textColor: "{colors.violet-light}"
    typography: "{typography.label}"
    rounded: "{rounded.pill}"
    padding: "12px 24px"
    height: "44px"
  button-play:
    backgroundColor: "{colors.karaoke}"
    textColor: "{colors.ink}"
    rounded: "{rounded.pill}"
    size: "64px"
  card-song:
    backgroundColor: "{colors.plum}"
    textColor: "{colors.ink}"
    rounded: "{rounded.lg}"
    padding: "12px"
  badge-level:
    backgroundColor: "{colors.amber}"
    textColor: "{colors.night}"
    typography: "{typography.label}"
    rounded: "{rounded.pill}"
    padding: "2px 10px"
  option-card:
    backgroundColor: "{colors.plum}"
    textColor: "{colors.ink}"
    typography: "{typography.title}"
    rounded: "{rounded.lg}"
    padding: "20px"
  option-card-selected:
    backgroundColor: "{colors.violet}"
    textColor: "{colors.ink}"
  sidebar:
    backgroundColor: "{colors.dusk}"
    textColor: "{colors.ink}"
    width: "264px"
  player-bar:
    backgroundColor: "{colors.dusk}"
    textColor: "{colors.ink}"
    height: "88px"
  input:
    backgroundColor: "{colors.dusk}"
    textColor: "{colors.ink}"
    typography: "{typography.body}"
    rounded: "{rounded.md}"
    padding: "12px 16px"
    height: "48px"
---

# Design System: lyricsync

## Overview

**Creative North Star: "A stage lit for one song"**

The room is dark and quiet; one song is playing, and the light follows the voice. Every screen is a night-time listening room: deep plum darkness, a few warm lights, and the line being sung is the brightest thing on the page. Colour is spent where the music is (the play button, the flowing timeline, the active line) and nowhere else. Everything else stays calm so the learner can listen.

This is deliberately **not** the usual language-app look. The common recipe for language learning (playful clay shapes, bright blocks, country flags, mascots, confetti) is what we reject; lyricsync should feel like a premium music app that happens to teach. Motion is part of the identity: smooth, unhurried, never bouncy, and always tied to sound.

The system has two modes:
- **Operate** for the app itself (home, library, the player, review, vocabulary, settings): clarity and consistency first, with brand in the details.
- **Persuade** for the public landing page, which may be bolder: the full-bleed cover glow and the moving wave as its hero.

**Key Characteristics:**
- Dark only (v1): `night` background, `dusk` frame, `plum` cards.
- Light comes from the music: karaoke pink glows only where sound is.
- The active lyric is white, large and underlined by a filling pink line.
- Nouns carry their gender in colour **and** an `m`/`f`/`n` marker.
- Smooth, spring-without-bounce motion; a living sine wave while playing, a straight line when paused.
- Flat surfaces, hairline borders, almost no shadows.

## Colors

A night palette of plum and violet, with warm pink for sound, amber for levels and teal for meaning.

### Primary
- **Violet** (`#8A37D2`): fills only: primary buttons, the selected onboarding card, active tab indicators, focus rings, switches when on. Never as text on a surface (2.34:1 on `plum` fails).
- **Violet Light** (`#B98AF0`): violet as text and icons: links, the active navigation label, secondary buttons (7.37:1 on `night`, 5.23:1 on `plum`).

### Secondary
- **Karaoke** (`#EE1B6C`): the colour of sound. The play button fill, the flowing timeline, the active line's underline, the breathing glow while playing, the streak flame. Never as body text; on it, only white icons (3.8:1 passes for icons, not for small text).
- **Amber** (`#FF871F`): CEFR level badges (`night` text on amber, 8.09:1), grammar notes, warnings. Not used for errors.

### Tertiary
- **Teal** (`#32989A`): bookmark and save icons, "saved" states on `night`.
- **Teal Light** (`#4FC1C3`): translation text, everywhere it appears on cards or the song page (9.02:1 on `night`, 6.41:1 on `plum`).

### Gender
Always paired with the marker, see Components → Gender token.
- **der, masculine** (`#6EA8FE`, blue): 8.05:1 on `night`, 5.72:1 on `plum`.
- **die, feminine** (`#FF7A8A`, coral): 7.78:1 / 5.53:1.
- **das, neuter** (`#8BD66B`, green): 11.04:1 / 7.84:1.

Under red-green colour blindness (deuteranopia, about 1 in 12 men) **die** and **das** both turn tan and look almost alike. That is why the marker is mandatory, not decoration. Learners can turn the colours off in Settings; the markers stay.

### Neutral
- **Night** (`#12081F`): the page background, the deepest layer. Also the first CSS rule on `html, body`, so there is never a white flash.
- **Dusk** (`#1E1030`): the frame: sidebar, mobile tab bar, player bar, inputs, sheets.
- **Plum** (`#411467`): elevated content: song cards, option cards, the word card, review cards.
- **Ink** (`#F5F3F7`): primary text and icons (17.63:1 on `night`).
- **Ink Muted** (`#B9AEC6`): secondary text, inactive lyric lines, timestamps at rest, placeholder text (9.19:1 on `night`, 6.53:1 on `plum`).
- **Hairline** (`rgba(245, 243, 247, 0.08)`): borders and dividers.
- **Error** (`#FF6B6B`): form and load errors, always with an alert icon and a sentence (7.01:1 on `night`, 6.46:1 on `dusk`). It only appears in forms and messages, never in lyrics, so it can't be mistaken for **die**.

### Named Rules
**The Sound Rule.** Karaoke pink appears only where music is: the play button, the timeline, the active line's underline, the playing glow. If nothing is playing, there is almost no pink on screen.

**The Fill-Not-Text Rule.** Violet and karaoke pink are fills. As text, use their light tints or `ink`.

**The Marker Rule.** Gender is never shown by colour alone.

## Typography

**Display Font:** Plus Jakarta Sans (fallback: system-ui, sans-serif)
**Body Font:** Inter (fallback: system-ui, sans-serif)
**Mono Font:** JetBrains Mono (fallback: ui-monospace, monospace)

**Character:** Plus Jakarta Sans is geometric with warm round shapes: confident for headings and big enough to sing along to. Inter carries everything you read quickly: translations, navigation, forms. JetBrains Mono keeps every number in its place.

All three are **self-hosted** (`@fontsource-variable`), never loaded from Google's servers: a German court ruled that this leaks visitors' IP addresses (GDPR). `font-display: swap`, with only the weights listed below loaded. All three support German characters (ä, ö, ü, ß, ẞ).

### Hierarchy
- **Display** (700, `clamp(2.25rem, 1.5rem + 3vw, 3.5rem)`, 1.1, -0.02em): onboarding questions, the landing hero, the song title on the song page.
- **Headline** (700, 1.75rem, 1.2): page titles ("Your A2 songs"), section heads on Home.
- **Title** (600, 1.125rem, 1.3): song card titles, card headings, option cards.
- **Lyric Active** (600, `clamp(1.75rem, 1.2rem + 2vw, 2.5rem)`, 1.25): the line being sung.
- **Lyric** (500, 1.25rem, 1.4): every other lyric line, in `ink-muted`.
- **Body** (Inter 400, 1rem, 1.5): running text, at most 65ch per line.
- **Translation** (Inter 400, 0.9375rem, 1.45): under each lyric line, in `teal-light`.
- **Label** (Inter 500, 0.8125rem, 0.01em): buttons, badges, navigation, form labels. Sentence case, no all-caps.
- **Mono** (JetBrains Mono 500, 0.8125rem, `font-variant-numeric: tabular-nums`): times (`1:24 / 3:05`), counters, review counts, streak numbers.

### Named Rules
**The Steady Numbers Rule.** Any number that changes while you watch (timers, counters) is set in JetBrains Mono with tabular figures, so nothing jumps.

**The One Voice Rule.** Only the active lyric is set at Lyric Active size. Nothing else on the song page may compete with it.

## Layout

**Grid and spacing.** A 4 px base, using the steps 4, 8, 12, 16, 24, 32, 48, 64. Content is at most 1200 px wide, centred; reading text at most 65ch.

**Breakpoints.**
- **Phone** (< 768 px): one column, bottom tab bar (64 px), mini player above it (64 px) while a song plays, 16 px side margins.
- **Tablet** (768–1023 px): collapsed sidebar (72 px, icons only), 24 px margins.
- **Desktop** (≥ 1024 px): full sidebar (264 px), 32 px margins.

**App shell.**
- **Desktop:** the sidebar on the left (`dusk`):
  - wordmark
  - navigation: Home, Library, Review, Vocabulary
  - the **account block** at the bottom:
    - display name, or "Guest"
    - level badge
    - streak with flame
    - daily-goal ring ("12 / 20")
    - "N reviews due"
    - Settings link
  - guests see *"Sign up to save words"* instead of streak and reviews
- **Player bar:** full width along the bottom (88 px) whenever a song is loaded.
- **Phone:** the tab bar has Home, Library, Review and Profile; the account block lives on Profile.

**Song card grid.** 2 columns on phones, 3 on tablets, 4 on desktop, 5 above 1440 px; 16 px gaps (24 px on desktop).

**Home, top to bottom.**
1. Welcome line: *"Welcome back, Anas"* (Headline), or *"Welcome back"* without a name.
2. **Continue listening**: one wide card with cover, title, progress.
3. **Reviews due**: a card with the count and **Start** (hidden for guests and when nothing is due).
4. **Songs for your level**: level tabs (A1–B2), the learner's own level selected, then the grid.
5. **Recently added**: a horizontal row.

**Song page.**
- **Header:**
  - *Desktop:* a 280 px square cover on the left; title (Display), artist (Title, `ink-muted`) and the level badge on the right.
  - *Phone:* the cover nearly full width (calc(100% − 32px)), title and artist below. As the lyrics scroll, the cover shrinks smoothly into a 40 px thumbnail in a compact header.
- **Behind everything:** the ambient glow (see Elevation).
- **Lyrics:** centred, max 720 px wide. The active line is kept at about 40 % of the viewport height by smooth auto-scroll; manual scrolling pauses auto-scroll until **"Back to the song"** is pressed.
- **Translation:** under each line, toggled in the player bar.
- **Player bar** (desktop and tablet):
  - small cover (48 px), title
  - the play button in the centre
  - the wave timeline with times on both sides
  - loop line, speed (1 / 0.85 / 0.75), translation toggle
- **Phone:** the controls sit in a sheet that expands from the mini player.

**Onboarding.** Full screen on `night`, no shell. One question centred (Display), options below, progress dots at the top, Back on the left, Skip on the right where allowed.

## Elevation & Depth

Depth comes from **tonal layering and light**, not shadows. Layers from back to front: `night` (page) → `dusk` (frame) → `plum` (content). Separation uses hairline borders.

- **Ambient glow:** on the song page, a radial gradient from the cover's dominant colour sits behind the header (top-centre, 70 % of the viewport wide, opacity 0.35, heavily blurred), fading into `night`. It crossfades over 600 ms when the song changes. Generated covers supply their gradient's main colour.
- **Playing glow:** `box-shadow: 0 0 24px 4px rgba(238, 27, 108, 0.45)` around the play button, breathing (see Motion).
- **Blur:** only the player bar, mini player and mobile tab bar use `backdrop-filter: blur(20px)` over `rgba(30, 16, 48, 0.82)` (`dusk`), so lyrics scrolling underneath stay faintly visible. Nowhere else.
- **Overlay shadow:** popovers and sheets only: `0 16px 48px rgba(0, 0, 0, 0.5)`.

### Named Rules
**The Flat-At-Rest Rule.** Surfaces are flat. Light appears only for sound (the playing glow), for focus, or for something floating above the page (popovers, sheets).

## Shapes

Soft and round, never sharp:
- song cards, option cards, the word card: **16 px**
- inputs, chips, small panels: **12 px**
- tooltips: **8 px**
- sheets and dialogs: **24 px** on their top corners
- buttons, badges, the play button, toggles, the mini player: **pill or circle**

Covers are rounded at 12 px in cards and 16 px on the song page, and are always **square** (1:1). Borders are 1 px `hairline`. Focus rings are 2 px `violet` with a 2 px offset, following the element's radius.

**Generated artwork** (for songs without an uploaded cover) is deterministic from the song's slug:
- the slug picks two palette colours (from violet, karaoke, plum, teal, amber), a gradient angle and one of six soft shapes (circle, arc, wave, bloom, stripe, orbit)
- it's drawn as SVG on `night`
- the same song always gets the same art
- the art never contains text

## Motion

Motion is part of lyricsync's identity: smooth, unhurried, never bouncy, always tied to the music. Everything below lives in shared motion tokens; components never invent their own timings.

### Tokens
| Token | Value | Use |
|---|---|---|
| `duration-instant` | 150 ms | hover, press, colour changes |
| `duration-standard` | 280 ms | panels, cards, toggles, the play-button morph |
| `duration-slow` | 450 ms | page transitions, the wave flattening, sheet open |
| `duration-ambient` | 600 ms | glow crossfade between songs |
| `ease-out` | `cubic-bezier(0.22, 1, 0.36, 1)` | anything entering or settling |
| `ease-in` | `cubic-bezier(0.4, 0, 1, 1)` | anything leaving (about 20 % shorter than its entrance) |
| `spring-soft` | stiffness 300, damping 30, mass 1 (no overshoot) | the play button, card lift, word-card pop |
| `stagger` | 30 ms per item, at most 8 items | lists and grids appearing |

### Signature motions
1. **Play ↔ pause morph.** The play triangle morphs into the two pause bars, as one SVG shape that changes its outline (both icons drawn with the same number of points), over `duration-standard` with `spring-soft`. It never swaps icons. Pressing scales the button to 0.94 for `duration-instant`.
2. **Breathing glow.** While playing, the play button's pink glow breathes: 2.4 s, `ease-in-out`, between 50 % and 100 % strength, looping. It stops (fades out over `duration-standard`) on pause.
3. **The wave timeline.**
   - **Playing:** the played part of the timeline is a **sine wave** in `karaoke`: 3 px stroke, round caps, wavelength 24 px, amplitude 3 px, flowing to the right one wavelength every 1.2 s.
   - **Unplayed part:** a straight 2 px line in `ink` at 20 % opacity.
   - **Pause:** the amplitude eases to **0** over `duration-slow` (`ease-out`), so the wave flattens into a **straight line**.
   - **Play again:** the amplitude grows back over the same time.
   - **Seeking:** a 12 px thumb appears on hover or drag, and the wave follows the thumb.
   - **Drawing:** SVG, redrawn with `requestAnimationFrame` only while visible and playing.
4. **The singing line.**
   - The active line grows from Lyric to Lyric Active size and from `ink-muted` to `ink` over `duration-standard`.
   - A 3 px `karaoke` underline fills from left to right over exactly the line's duration (linear, because it is time).
   - Lines that are done fade back to `ink-muted`.
   - Auto-scroll moves smoothly over `duration-slow` (`ease-out`).
5. **Ambient glow** crossfades between songs over `duration-ambient`.

### Everyday motion
- **Pages:** fade in and rise 8 px over `duration-slow` (`ease-out`); leaving pages simply fade out (`ease-in`, 200 ms).
- **Lists and grids:** items fade in and rise 8 px, staggered.
- **Song cards:**
  - hover lifts the cover 4 px and brightens its border, over `duration-instant`
  - the cover zooms 1.03 inside its frame
- **Word card:** pops from the tapped word (scale 0.96 → 1, fade) with `spring-soft`; on phones the bottom sheet slides up over `duration-slow`.
- **Onboarding:**
  - steps slide sideways: the next step enters from 24 px right, the previous one leaves 24 px left
  - the selected option card fills violet and shows its check with `spring-soft`
- **Mobile cover shrink:** the song header cover scales and moves into the compact header, driven by scroll position (no timing), ending at 40 px.
- **Skeletons:** a soft shimmer sweeping across every 1.6 s.

### Reduced motion
When `prefers-reduced-motion: reduce` is set:
- every movement becomes a simple crossfade of at most 150 ms
- the wave is always a **straight line**, the play icon crossfades instead of morphing, and the glow is static
- auto-scroll jumps instead of gliding, and the cover shrink becomes a crossfade
- nothing loops

### Named Rules
**The Music Moves Rule.** Only things tied to sound may loop: the wave and the play-button glow, and only while playing.

**The No-Bounce Rule.** Springs settle without overshoot. Nothing wobbles, jiggles or bounces.

**The No-Hijack Rule.** Never take over scrolling, never parallax the page, never animate on scroll except the cover shrink.

## Components

### Buttons
- **Shape:** pill (`rounded.pill`), 44 px high (48 px on phones), at least 44 × 44 px to tap.
- **Primary:** `violet` fill, `ink` label (5.35:1). Hover: 8 % lighter, over `duration-instant`; press: scale 0.97.
- **Secondary:** transparent with a 1 px `hairline` border, `violet-light` label. Hover: `plum` fill.
- **Ghost:** text only, `violet-light`.
- **Loading:** the label stays and a small spinner replaces the icon (the only place a spinner is allowed).
- **Disabled:** 40 % opacity, no hover.

### Play button (signature)
64 px circle (56 px in the mini player, 48 px in the compact header), `karaoke` fill, white icon (24 px). Morph and glow: see Motion. Space bar toggles play anywhere on the song page. Accessible name changes: "Play" / "Pause".

### Wave timeline (signature)
Full width of the player bar's centre column, 32 px tall hit area (44 px on phones), drawing area 8 px. Times in `mono` on both sides: elapsed in `ink`, total in `ink-muted`.
- **Click or drag:** seeks.
- **Keyboard:** ←/→ seek ±5 s, Home/End jump to start/end.
- **Screen readers:** `role="slider"` with `aria-valuetext="1 minute 24 seconds of 3 minutes 5 seconds"`.

### Song card
- **Layout:** a square cover on top (12 px radius), then the title (Title, up to 2 lines) and the **level badge**.
- **Excluded on purpose:** **no artist name**; the artist appears on the song page.
- **Completed songs:** a small `teal` check on the cover's corner.
- **Background:** `plum`, 12 px padding, 16 px radius, 1 px `hairline` border.
- **Behaviour:** the whole card is one link.

### Level badge
`amber` pill with `night` text (Label): "A1", "A2", "B1", "B2". On covers, it sits on a `night` 70 % backdrop for contrast.

### Gender token and marker
A noun in the lyrics is a button (like every word) whose text takes its gender colour. A small superscript marker follows it:
- **m** for der, **f** for die, **n** for das
- Inter 600, 0.625em, same colour
- also read by screen readers as "masculine", "feminine", "neuter"

A **legend** in the song page header ("der m · die f · das n") explains it and links to Settings, where the colours can be switched off. The markers stay either way. Plurals use their singular's gender.

### Lyric line
Each word is a `<button>` (tap, click, Enter/Space): no visible button styling, a 2 px `violet` focus ring and a faint `plum` hover background. Separable verb partners highlight together when either is tapped. The translation sits below each line in `teal-light` (Translation style).

### Word card
- **Shape:** `plum`, 16 px radius, max 360 px wide; a popover on desktop, a bottom sheet on phones.
- **Contents:**
  - the word as sung
  - the **lemma** with its article in the gender colour plus marker ("das Herz n")
  - word type
  - 1–3 meanings
  - **Save to review** (primary) and **Replay line** (secondary)
- **Behaviour:** opening pauses the audio.
- **When saved:** the save icon turns `teal` and a toast confirms ("Saved: das Herz").

### Option card (onboarding)
`plum`, 16 px radius, 20 px padding, Title text plus a short description in `ink-muted`.
- **Selected:** `violet` fill with a check circle top-right.
- **Layout:** 2 × 2 grid on desktop, stacked on phones.
- **Multi-select** ("Why German?"): checkboxes semantically, same visuals.

### Navigation
- **Sidebar items:** icon (Lucide, 20 px, 1.5 px stroke) plus Label.
  - active: `violet-light` text and a 3 px `violet` bar on the left
  - hover: `plum` 50 % background
- **Tab bar items:** icon plus a small label; active in `violet-light`.

### Account block
Bottom of the sidebar or on Profile:
- **Top row:** display name in Title (or "Guest"), with the level badge.
- **Stats:** the streak (flame icon in `karaoke` with a `mono` number), and a daily-goal **ring** (32 px, `violet` progress on a `hairline` track, "12 / 20" in `mono`).
- **Reviews:** "N reviews due" as a link.
- **Guests:** *"Sign up to save words"* (primary button) instead of the stats.

### Inputs / Fields
`dusk` background, 1 px `hairline` border, 12 px radius, 48 px high, Body text, labels above in Label.
- **Focus:** border becomes `violet` plus the focus ring.
- **Error:** message below in `#FF6B6B` with an icon, and `aria-describedby`.
- **Disabled:** 40 % opacity.

### Toasts
Bottom-centre, above the player. `plum`, 12 px radius, an icon plus one line. Enter with `spring-soft`, leave after 3 s with a fade. Never for errors that need action.

### Skeletons
Shapes of the real content (cover squares, two text lines) in `plum` at 60 % with the shimmer. Never spinners for page loads.

### Empty and error states
One calm line plus one action, centred, with a soft generated-art graphic.
- **Vocabulary empty:** *"Tap any word in a song to save it."* → **Browse songs**
- **Review empty:** *"Nothing due. Come back tomorrow, or play a song."* → **Play a song**
- **Error:** *"We couldn't load this song."* → **Try again**. Never a raw error code.
- **Offline:** a slim notice at the top: *"You're offline. Songs you've opened still play."*

### Cover
Square, `object-fit: cover`. Uploaded covers are served from R2 with long caching. Without one, the generated artwork (see Shapes). Alt text: the song title.

## Pages

- **Onboarding** (4 steps): name (optional, "Skip") → level (required) → why German (several, "Skip") → daily time ("Skip"). The last screen says *"Your A2 songs are ready, Anas"* and fades into Home. Answers live in the browser until sign-up.
- **Home**, **Library** (level tabs plus the grid, filter by completed), **Song**: as in Layout.
- **Review:**
  - one card at a time (`plum`); the front shows the word with its article, and the line's audio plays
  - **Show** reveals meanings and the line with the word highlighted
  - the grade bar (Again, Hard, Good, Easy) uses label buttons with `mono` intervals beneath
  - the summary at the end: *"Nice work, Anas: 12 reviewed"*
- **Vocabulary:** a list of saved words (gender-coloured lemma plus marker, meanings, sightings count), search, sort.
- **Profile / Settings:**
  - display name, level, daily time, learning reasons, gender colours on or off
  - playback defaults
  - export data, delete account
- **Sign up / Log in / Verify email / Reset password:** centred single column, 400 px max, wordmark on top. Sign up is prefilled with the onboarding name.
- **Privacy, Impressum:** Read mode, 65ch columns, Body text.
- **404:** a generated-art graphic, *"This page isn't playing."* → **Go home**.
- **Admin** (Sprint 6): the same tokens in a plain work-tool style: dense tables, no ambient glow, no signature motions except the play button.

## Do's and Don'ts

### Do:
- **Do** set `html, body { background: #12081F }` as the very first CSS rule.
- **Do** keep karaoke pink to sound (The Sound Rule) and violet to fills (The Fill-Not-Text Rule).
- **Do** pair every gender colour with its `m`/`f`/`n` marker.
- **Do** use the motion tokens for every transition and respect `prefers-reduced-motion`.
- **Do** set times and counters in JetBrains Mono with tabular figures.
- **Do** keep every tap target at least 44 × 44 px and every interactive element keyboard-reachable with a visible focus ring.
- **Do** use the display name sparingly: welcome, profile, review summary, sign-up prefill.
- **Do** check every screen at 390 px and 1440 px before calling it done.

### Don't:
- **Don't** show the artist on song cards.
- **Don't** use album art or artist photos without a license; use uploaded or generated covers.
- **Don't** load fonts or any asset from Google or other third-party servers at runtime.
- **Don't** use confetti, mascots, cartoon characters, country flags or claymorphism.
- **Don't** bounce, wobble or overshoot; don't loop anything that isn't tied to playing sound.
- **Don't** hijack scrolling or add parallax.
- **Don't** put violet or karaoke pink text on `plum`.
- **Don't** use spinners for page loads (skeletons instead) or show raw error codes.
- **Don't** add a light theme in v1.
