# web

The lyricsync web app: Next.js API on [vinext](https://github.com/cloudflare/vinext), running on Cloudflare Workers ([ADR-0012](../docs/adr/0012-web-on-cloudflare-workers.md)). Design rules and tokens come from [DESIGN.md](../DESIGN.md); `app/theme.css` mirrors its tokens and a test keeps the two in sync.

Needs Node 22.23 or newer.

## Scripts

| Script | What it does |
|---|---|
| `npm run dev` | Dev server in `workerd`; `/api` and `/media` are proxied to the compose stack on `localhost:${HTTP_PORT:-80}` |
| `npm test` | Vitest unit tests |
| `npm run test:e2e` | Playwright at 390 px and 1440 px (starts the dev server) |
| `npm run lint` / `npm run typecheck` | ESLint and `tsc` |
| `npm run build` | Builds the Worker |
| `npm run deploy` | Deploys to Cloudflare (CI does this from `main`) |

## Layout

- `app/`: routes, the root layout with the app shell, `theme.css` (tokens) and `globals.css`
- `components/shell/`: sidebar, tab bar, account block, page transition
- `lib/`: motion tokens, the TanStack Query client, `cn`
- `e2e/`: Playwright tests
