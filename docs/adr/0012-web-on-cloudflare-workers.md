# Web app on Cloudflare Workers with vinext

The Next.js app in `web/` is built with vinext (the Next.js API on Vite) and runs on Cloudflare Workers, not as a container on the VPS. The domain is proxied by Cloudflare: requests for `/api/*` (and `/media/*` in development) go to the VPS through Traefik, everything else to the `lyricsync-web` Worker. App and API share one origin, so the auth cookies stay `httpOnly` and `SameSite=Lax`, and there is no CORS.

Pages render on the Worker, but data that changes per learner is fetched in the browser with TanStack Query. That keeps each request under the free plan's 10 ms CPU limit, and the VPS keeps its memory for the backend, identity and NLP services.

## Considered Options

- **Next.js container on the VPS behind Traefik:** the original plan. It costs about 300 MB of RAM on a 8 GB server shared with three JVM/Python services, and every page request crosses the ocean to one machine in Germany.
- **OpenNext on Workers:** builds with the real Next.js compiler, then adapts the output. Cloudflare now recommends vinext for new projects; it runs in `workerd` during development too, so local and production behave the same.
- **Static export on Cloudflare Pages:** no server rendering and no route handlers, and Pages is being folded into Workers.

## Consequences

- Worker settings live in `web/cloudflare.config.ts` (vinext's `cf` config), not `wrangler.jsonc`; deploys run `vinext-cloudflare deploy` from GitHub Actions with a `CLOUDFLARE_API_TOKEN` secret ("Edit Cloudflare Workers" template).
- Until the domain is set up, the app lives at `lyricsync-web.<account>.workers.dev`, where `/api` is not routed yet. The edge route `/api/*` → VPS is part of the launch setup.
- In development, Vite proxies `/api` and `/media` to the local compose stack, so the browser code never needs to know the API's host.
- vinext does not reproduce every Next.js feature. `next/font` loads Google Fonts from Google's CDN, which [ADR-0010](0010-no-external-calls-at-runtime.md) rules out, so fonts come from `@fontsource-variable` packages bundled into our own assets. `next/image` build-time optimisation is not used; covers come from R2 at their final size.
- The VPS no longer runs a `web` container; Traefik routes only API traffic.
