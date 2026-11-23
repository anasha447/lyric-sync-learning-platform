import http from "node:http";
import type { Connect, Plugin } from "vite";

const BACKEND_PATH = /^\/(api|media)(\/|\?|$)/;

/** True for the paths the edge sends to the VPS: /api and /media, not e.g. /apis or /library. */
export const isBackendPath = (url: string) => BACKEND_PATH.test(url);

/**
 * Plays the Cloudflare edge's part locally (ADR-0012): /api and /media go to the backend, the
 * rest to the Worker. Vite's own `server.proxy` can't do this here, because the Cloudflare plugin
 * answers every request in the Worker first; this middleware is registered before it, for both
 * `vite dev` and `vite preview`.
 */
export function edgeRouting(backendOrigin: string): Plugin {
  const forward: Connect.NextHandleFunction = (req, res, next) => {
    if (!req.url || !isBackendPath(req.url)) return next();
    const url = new URL(req.url, backendOrigin);
    const upstream = http.request(url, { method: req.method, headers: { ...req.headers, host: url.host } }, (answer) => {
      res.writeHead(answer.statusCode ?? 502, answer.headers);
      answer.pipe(res);
    });
    upstream.on("error", () => {
      res.statusCode = 502;
      res.end(`The backend isn't reachable at ${backendOrigin}. Is the compose stack running?`);
    });
    req.pipe(upstream);
  };

  return {
    name: "lyricsync-edge-routing",
    enforce: "pre",
    configureServer: (server) => void server.middlewares.use(forward),
    configurePreviewServer: (server) => void server.middlewares.use(forward),
  };
}
