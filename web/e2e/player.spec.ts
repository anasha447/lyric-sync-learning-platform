import { expect, test, type Page } from "@playwright/test";

/** A silent 16-bit mono WAV, so the test plays real audio without shipping a file. */
function silentWav(seconds: number, sampleRate = 8_000): Buffer {
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

const song = {
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

async function mockApi(page: Page) {
  let songRequests = 0;
  await page.route("**/api/songs/test-song", (route) => {
    songRequests++;
    return route.fulfill({ json: song });
  });
  await page.route("**/api/songs/missing", (route) => route.fulfill({ status: 404, json: { status: 404 } }));
  await page.route("**/media/audio/test.wav", (route) =>
    route.fulfill({ body: silentWav(6), contentType: "audio/wav" }),
  );
  return () => songRequests;
}

test("the lines light up in time with the song", async ({ page }) => {
  await mockApi(page);
  await page.goto("/songs/test-song");
  const lyrics = page.getByRole("list", { name: "Lyrics" });
  await expect(page.getByRole("heading", { level: 1, name: "Das Testlied" })).toBeVisible();
  await expect(lyrics.getByText("First line")).toBeVisible();

  await page.getByRole("button", { name: "Play" }).click();
  const current = lyrics.locator('li[aria-current="true"]');
  await expect(current).toContainText("Erste Zeile");
  // Playback can start a few seconds after the click while the browser loads the file;
  // measure the song from when the audio actually runs.
  await expect
    .poll(() => page.evaluate(() => document.querySelector("audio")!.currentTime), { timeout: 10_000 })
    .toBeGreaterThan(0.1);
  await expect(current).toContainText("Zweite Zeile", { timeout: 4_000 });
  await expect(lyrics.locator('li[data-state="done"]').first()).toContainText("Erste Zeile");
});

test("scrolling away pauses auto-scroll until Back to the song", async ({ page }) => {
  await mockApi(page);
  await page.goto("/songs/test-song");
  await page.getByRole("button", { name: "Play" }).click();
  await expect(page.locator('li[aria-current="true"]')).toContainText("Erste Zeile");

  const back = page.getByRole("button", { name: "Back to the song" });
  await expect(back).toHaveCount(0);
  await page.mouse.move(200, 400);
  await page.mouse.wheel(0, 400);
  await expect(back).toBeVisible();
  await back.click();
  await expect(back).toHaveCount(0);
});

test("going back to a song costs no second request", async ({ page }) => {
  const songRequests = await mockApi(page);
  await page.goto("/songs/test-song");
  await expect(page.getByRole("heading", { level: 1, name: "Das Testlied" })).toBeVisible();

  await page.getByRole("navigation", { name: "Main" }).filter({ visible: true }).getByRole("link", { name: "Library" }).click();
  await expect(page.getByRole("heading", { level: 1, name: "Library" })).toBeVisible();
  await page.goBack();
  await expect(page.getByRole("heading", { level: 1, name: "Das Testlied" })).toBeVisible();
  expect(songRequests()).toBe(1);
});

test("a song that doesn't exist shows the 404 page, not Try again", async ({ page }) => {
  await mockApi(page);
  await page.goto("/songs/missing");
  await expect(page.getByRole("heading", { name: "This page isn't playing." })).toBeVisible();
  await expect(page.getByRole("button", { name: "Try again" })).toHaveCount(0);
});
