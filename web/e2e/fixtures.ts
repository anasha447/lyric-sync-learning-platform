import { expect, type Page, type TestInfo } from "@playwright/test";

/** A silent 16-bit mono WAV, so tests play real audio without shipping a file. */
export function silentWav(seconds: number, sampleRate = 8_000): Buffer {
  const samples = seconds * sampleRate;
  const buffer = Buffer.alloc(44 + samples * 2);
  buffer.write("RIFF", 0);
  buffer.writeUInt32LE(36 + samples * 2, 4);
  buffer.write("WAVEfmt ", 8);
  buffer.writeUInt32LE(16, 16);
  buffer.writeUInt16LE(1, 20); // PCM
  buffer.writeUInt16LE(1, 22); // mono
  buffer.writeUInt32LE(sampleRate, 24);
  buffer.writeUInt32LE(sampleRate * 2, 28);
  buffer.writeUInt16LE(2, 32);
  buffer.writeUInt16LE(16, 34);
  buffer.write("data", 36);
  buffer.writeUInt32LE(samples * 2, 40);
  return buffer;
}

export const testSong = {
  slug: "test-song",
  title: "Das Testlied",
  artist: "Test Artist",
  level: "A2",
  durationMs: 6_000,
  audioUrl: "/media/audio/test.wav",
  lines: [
    { index: 0, text: "Erste Zeile", startMs: 0, endMs: 1_500, instrumental: false, translation: "First line" },
    { index: 1, text: "Zweite Zeile", startMs: 1_500, endMs: 3_000, instrumental: false, translation: "Second line" },
    { index: 2, text: "", startMs: 3_000, endMs: 4_500, instrumental: true, translation: null },
    { index: 3, text: "Letzte Zeile", startMs: 4_500, endMs: 6_000, instrumental: false, translation: null },
  ],
};

/** Serves the test song, a 404 for "missing", and the audio; returns how often the song was fetched. */
export async function mockApi(page: Page) {
  let songRequests = 0;
  await page.route("**/api/songs/test-song", (route) => {
    songRequests++;
    return route.fulfill({ json: testSong });
  });
  await page.route("**/api/songs/missing", (route) => route.fulfill({ status: 404, json: { status: 404 } }));
  const audio = silentWav(6);
  await page.route("**/media/audio/test.wav", (route) => route.fulfill(rangeResponse(audio, route.request().headers().range)));
  return () => songRequests;
}

/**
 * Answers Range requests with 206, as R2 and our /media do. Without it a browser can't
 * seek into the part of the file it hasn't downloaded yet.
 */
function rangeResponse(body: Buffer, range: string | undefined) {
  const headers = { "Accept-Ranges": "bytes", "Content-Type": "audio/wav" };
  const match = range && /^bytes=(\d+)-(\d*)$/.exec(range);
  if (!match) return { status: 200, headers, body };
  const start = Number(match[1]);
  const end = match[2] ? Math.min(Number(match[2]), body.length - 1) : body.length - 1;
  return {
    status: 206,
    headers: { ...headers, "Content-Range": `bytes ${start}-${end}/${body.length}` },
    body: body.subarray(start, end + 1),
  };
}

/** The audio's current time in seconds. */
export const audioTime = (page: Page) => page.evaluate(() => document.querySelector("audio")!.currentTime);

/** Where the controls are: the player bar, or on phones the sheet opened from the mini player. */
export async function openControls(page: Page, testInfo: TestInfo) {
  if (testInfo.project.name === "phone") {
    await page.getByRole("button", { name: /Open the player/ }).click();
    const sheet = page.getByRole("dialog", { name: "Player" });
    // the sheet slides up over duration-slow: wait until it stops moving before measuring anything in it
    let lastY = Number.NaN;
    await expect
      .poll(async () => {
        const y = (await sheet.boundingBox())?.y ?? Number.NaN;
        const settled = y === lastY;
        lastY = y;
        return settled;
      })
      .toBe(true);
    return sheet;
  }
  return page.getByRole("region", { name: "Player" });
}
