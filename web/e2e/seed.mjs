// Seeds one published song into the compose stack for the end-to-end test against the real backend.
//   docker compose --profile app up -d --wait   (from the repo root)
//   node e2e/seed.mjs                           (from web/)
// Idempotent: a song that's already published is reused.
import { execFileSync } from "node:child_process";
import { writeFileSync } from "node:fs";
import { fileURLToPath } from "node:url";

const API = process.env.LYRICSYNC_API_ORIGIN ?? `http://localhost:${process.env.HTTP_PORT ?? "80"}`;
const REPO_ROOT = fileURLToPath(new URL("../..", import.meta.url));
const OUT = fileURLToPath(new URL("./.seeded-song.json", import.meta.url));
const TITLE = "Das Testlied";
const DURATION_MS = 8_000;

/**
 * A silent MP3 (MPEG-1 Layer III, 128 kbit/s, 44.1 kHz, mono). Every frame is a 4-byte header
 * followed by zeros, and all-zero frame data decodes to silence, so no binary file is needed.
 */
function silentMp3(seconds) {
  const frameBytes = 417; // 144 × 128 000 / 44 100, no padding
  const frames = Math.ceil(seconds / (1152 / 44_100));
  const mp3 = Buffer.alloc(frames * frameBytes);
  for (let i = 0; i < frames; i++) mp3.set([0xff, 0xfb, 0x90, 0xc0], i * frameBytes);
  return mp3;
}

const sql = (query) =>
  execFileSync(
    "docker",
    ["compose", "exec", "-T", "postgres", "psql", "-U", "lyricsync", "-d", "lyricsync", "-tA", "-v", "ON_ERROR_STOP=1", "-c", query],
    { cwd: REPO_ROOT, encoding: "utf8" },
  ).trim();

async function waitForApi() {
  for (let i = 0; i < 60; i++) {
    try {
      if ((await fetch(`${API}/api/songs`)).ok) return;
    } catch {
      // not up yet
    }
    await new Promise((r) => setTimeout(r, 2_000));
  }
  throw new Error(`The backend at ${API} didn't come up`);
}

let slug = sql(`SELECT slug FROM catalog.song WHERE title = '${TITLE}' AND status = 'PUBLISHED' LIMIT 1`);

if (!slug) {
  const form = new FormData();
  form.append("audio", new Blob([silentMp3(DURATION_MS / 1000)], { type: "audio/mpeg" }), "testlied.mp3");
  form.append("lrcText", "[00:00.50]Erste Zeile\n[00:02.00]Zweite Zeile\n[00:04.00]Dritte Zeile\n[00:06.00]Letzte Zeile\n");
  form.append("title", TITLE);
  form.append("artistName", "lyricsync tests");
  form.append("cefrLevel", "A2");
  form.append("licenseType", "own_recording");
  form.append("licenseNote", "Generated silence for the end-to-end test.");
  form.append("durationMs", String(DURATION_MS));

  const response = await fetch(`${API}/api/admin/songs`, { method: "POST", body: form });
  if (response.status !== 201) throw new Error(`Ingesting the test song failed: ${response.status} ${await response.text()}`);
  slug = (await response.json()).slug;

  // There's no publish endpoint until the admin review (Sprint 6), so publish in SQL. That skips the
  // SongPublished event that clears the backend's song caches, so restart it to start them empty.
  sql(`UPDATE catalog.song SET status = 'PUBLISHED', published_at = now() WHERE slug = '${slug}'`);
  execFileSync("docker", ["compose", "--profile", "app", "restart", "backend"], { cwd: REPO_ROOT, stdio: "ignore" });
  await waitForApi();
}

writeFileSync(OUT, JSON.stringify({ slug, title: TITLE }, null, 2));
console.log(`Seeded song: ${slug}`);
