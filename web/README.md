# web

The lyricsync web app: Next.js API on [vinext](https://github.com/cloudflare/vinext), running on Cloudflare Workers ([ADR-0012](../docs/adr/0012-web-on-cloudflare-workers.md)). Design rules and tokens come from [DESIGN.md](../DESIGN.md); `app/theme.css` mirrors its tokens and a test keeps the two in sync.

Needs Node 22.23 or newer.

## Scripts

| Script | What it does |
|---|---|
| `npm run dev` | Dev server in `workerd`; `/api` and `/media` go to the compose stack on `localhost:${HTTP_PORT:-80}` (`edge-routing.ts`, the local stand-in for the Cloudflare edge) |
| `npm test` | Vitest unit tests |
| `npm run test:e2e` | Playwright at 390 px and 1440 px against a mocked API (starts the dev server; `E2E_TARGET=preview` tests the built Worker) |
| `npm run lint` / `npm run typecheck` | ESLint and `tsc` |
| `npm run build` | Builds the Worker |
| `npm run deploy` | Deploys to Cloudflare (CI does this from `main`) |

## End-to-end against the real backend

`e2e/backend.spec.ts` opens the library, opens a seeded song and checks its lyrics light up, with no mocks. From the repo root, then `web/`:

```bash
docker compose --profile app up -d --wait
node e2e/seed.mjs          # ingests and publishes "Das Testlied" (a generated silent MP3)
npm run build
E2E_BACKEND=1 E2E_TARGET=preview npx playwright test e2e/backend.spec.ts
```

CI runs the same steps in the "Web end-to-end against the backend" job.

## Layout

- `app/`: routes, the root layout with the app shell, `theme.css` (tokens) and `globals.css`
- `components/shell/`: sidebar, tab bar, account block, page transition
- `components/player/`: lyrics, play button, wave timeline, player bar and phone sheet
- `components/songs/`: song card, cover and generated artwork, library, song header
- `lib/`: motion tokens, the TanStack Query client and song queries, player logic, `cn`
- `e2e/`: Playwright tests, shared fixtures and the backend seed
