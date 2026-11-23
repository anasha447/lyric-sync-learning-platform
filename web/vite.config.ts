import { defineConfig } from "vite";
import vinext from "vinext";
import { cloudflare } from "@cloudflare/vite-plugin";
import { edgeRouting } from "./edge-routing";

// The local compose stack (Traefik on HTTP_PORT). In production the Cloudflare edge routes
// /api/* to the VPS on the same origin (ADR-0012), so the browser code only ever calls "/api".
const backendOrigin = process.env.LYRICSYNC_API_ORIGIN ?? `http://localhost:${process.env.HTTP_PORT ?? "80"}`;

export default defineConfig({
  plugins: [
    edgeRouting(backendOrigin),
    vinext(),
    cloudflare({
      viteEnvironment: {
        name: "rsc",
        childEnvironments: ["ssr"],
      },
    }),
  ],
});
