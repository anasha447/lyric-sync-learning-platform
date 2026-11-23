import { defineConfig } from "vite";
import vinext from "vinext";
import { cloudflare } from "@cloudflare/vite-plugin";

// The local compose stack (Traefik on HTTP_PORT). In production the Cloudflare edge routes
// /api/* to the VPS on the same origin (ADR-0012), so the browser code only ever calls "/api".
const apiOrigin = process.env.LYRICSYNC_API_ORIGIN ?? `http://localhost:${process.env.HTTP_PORT ?? "80"}`;

export default defineConfig({
  plugins: [
    vinext(),
    cloudflare({
      viteEnvironment: {
        name: "rsc",
        childEnvironments: ["ssr"],
      },
    }),
  ],
  server: {
    proxy: {
      "/api": { target: apiOrigin, changeOrigin: true },
      "/media": { target: apiOrigin, changeOrigin: true },
    },
  },
});
