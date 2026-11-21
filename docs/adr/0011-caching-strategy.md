# Caching strategy

Cache data that is reused and rarely changes, as close to the learner as possible, and never cache one learner's data on the server. A learner who goes from song A to song B and back to A should download nothing again.

| Layer | What | How | Invalidated by |
|---|---|---|---|
| Browser + CDN | Song audio | Public R2 bucket on a custom domain behind Cloudflare's CDN. Object key is the file's SHA-256 (`audio/<hash>.mp3`), served with `Cache-Control: public, max-age=31536000, immutable` | Never: a new upload gets a new key |
| Browser + CDN | Published song JSON, library pages | `ETag` from the song's version and `Cache-Control: public, max-age=60, stale-while-revalidate=600`; Cloudflare caches `/api/songs*` | Version bump on republish |
| Browser memory | Songs, library, learner data | TanStack Query: published songs never go stale in a session; learner queries are refetched after each mutation | Mutations, reload |
| Backend memory | Assembled song responses, library pages, frequent dictionary forms during ingestion | Spring Cache with Caffeine, size-bounded, hit ratio exported through Micrometer | `SongPublished`, `SongReanalysed`, `SongUnpublished` events |
| None | Due reviews, saved words, streaks, settings | `Cache-Control: private, no-store` | — |

This replaces the presigned audio URLs of [ADR-0005](0005-audio-on-r2.md). A new signature on every request gives the same file a new URL, which defeats both the browser and the CDN cache. Public URLs are acceptable because every published song is openly licensed ([ADR-0008](0008-license-policy.md)). Content-hash keys can't be guessed, and draft songs are never listed. If commercially licensed songs arrive later, the CDN can serve them through signed URLs whose cache key ignores the signature.

## Considered Options

- **Presigned URLs, reused server-side for their lifetime:** caching works only while the same URL is handed out, it breaks at expiry, and the CDN can't share it between learners.
- **Redis:** one more container and memory budget, and its benefit, a cache shared by several backend instances, doesn't apply to a single instance. Spring Cache keeps the switch a configuration change if replicas arrive.
- **Hibernate second-level cache:** caches entities rather than the response learners actually request, and its invalidation is harder to reason about.

## Consequences

- The song response carries `audioUrl`; the separate `GET /api/songs/{id}/audio-url` endpoint is dropped.
- `song.audio_key` holds the content hash and `song.version` increases on every republish.
- The development storage adapter sends the same caching and range headers as R2.
- Cloudflare cache rules for audio, `/api/songs*` and Next.js static assets are part of the launch setup.
