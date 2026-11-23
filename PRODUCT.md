# Product
<!-- impeccable:product-schema 1 -->

## Platform
web

## Stack
Next.js API on vinext, deployed to Cloudflare Workers (ADR-0012); TypeScript, Tailwind, shadcn/ui, TanStack Query, Motion. The backend (Spring Modulith, identity-service, nlp-service) runs on a VPS behind Cloudflare; app and API share one origin.

## Users
English speakers learning German at levels A1 to B2, who would rather learn from music than from drills. They listen on a phone on the way somewhere or at a desk with headphones, in short sessions of 5 to 20 minutes. Many arrive with a concrete reason: moving to or living in a German-speaking country, work, studies, or a love of the music and culture.

A **guest** can listen and tap words without an account. A **learner** has an account and saves words for review. An **admin** adds and publishes songs.

## Product Purpose
lyricsync turns German songs into lessons: the lyrics light up line by line in time with the music, every word can be tapped for its meaning, base form and gender, and saved words come back in spaced-repetition reviews that replay the exact line they were heard in. Success is a learner who comes back daily, remembers words because they remember the song, and moves up a level.

## Positioning
The review replays the learner's own moment: the same song, the same line, the same voice. Vocabulary apps show a word on a card; lyricsync brings back the bar of music it was learned in. The whole product is built around listening first, not around a course.

## Operating Context
- **Onboarding, first visit, as a guest:** four screens, one question each:
  1. *"What should we call you?"*: optional display name, up to 30 characters, skippable
  2. *Level*, required, one of four: A1 "I'm just starting", A2 "I know the basics", B1 "I can hold a conversation", B2 "I'm comfortable"
  3. *Why German?*, several allowed, skippable: moving to or living in a German-speaking country; work or career; studies; music, culture and travel
  4. *Daily time*, skippable: 5, 10, 15 or 20 minutes a day, stored as a daily goal of 10, 20, 30 or 40 reviews

  Answers live in the browser until sign-up, then move to the learner. All of them can be changed in Settings.
- **Home:** a welcome line using the display name, continue listening, reviews due, songs for the learner's level, recently added.
- **A song:** play it, read along, loop a line, slow it down, tap words, save them.
- **Review:** hear the line again, recall the word, grade it.
- **Admin:** upload audio, `.lrc` lyrics, a cover and its note; check the sync; fix words; publish.

## Capabilities and Constraints
- Levels are A1, A2, B1 and B2; nothing above B2 in v1.
- The interface is in English; German appears only in the content (lyrics, words, examples).
- Nouns show their gender (der, die, das) by colour **and** an `m`/`f`/`n` marker.
- Songs are published only with a license that allows translation and commercial use (ADR-0008). Covers are uploaded by the admin (AI-generated art is fine) with an optional note; songs without a cover get generated artwork.
- No external calls at runtime on behalf of a learner (ADR-0010); fonts are self-hosted.
- Free tiers only, apart from the VPS and the domain.

## Brand Commitments
- Name: **lyricsync**, always lowercase.
- Voice: warm, short and encouraging; speaks to the learner as "you"; uses their display name sparingly ("Welcome back, Anas"). No exclamation-mark spam, no confetti, no mascots, no childish gamification. It should feel like a calm, premium music app, not a game.
- The owner set the palette, the der/die/das colours, the fonts and the signature motions; DESIGN.md records them.

## Evidence on Hand
- Synced `.lrc` files for the first songs.
- No testimonials, users, ratings or press yet: never invent them.
- Seed content is German folk songs with public-domain lyrics and openly licensed recordings.

## Product Principles
1. **Listening first.** The song is the lesson; every screen serves hearing, reading and understanding it.
2. **Your own moment comes back.** Reviews replay the line a word was saved from.
3. **Calm progress.** Encourage, never pressure; streaks and goals inform, they don't nag.
4. **Honest content.** Only licensed songs, real data, no invented social proof.
5. **Works without an account.** A guest gets the full listening experience; an account only adds saving and reviews.

## Accessibility & Inclusion
WCAG 2.2 AA: text contrast at least 4.5:1, keyboard access to the player and every word, visible focus, never colour alone (gender markers), `prefers-reduced-motion` respected everywhere, captions are the product itself (lyrics with translation).
