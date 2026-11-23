import { expect, test } from "@playwright/test";
import { audioTime, mockApi } from "./fixtures";

test("the lines light up in time with the song", async ({ page }) => {
  await mockApi(page);
  await page.goto("/songs/test-song");
  const lyrics = page.getByRole("list", { name: "Lyrics" });
  await expect(page.getByRole("heading", { level: 1, name: "Das Testlied" })).toBeVisible();
  await expect(lyrics.getByText("First line")).toBeVisible();

  await page.getByRole("button", { name: "Play", exact: true }).click();
  const current = lyrics.locator('li[aria-current="true"]');
  await expect(current).toContainText("Erste Zeile");
  // Playback can start a few seconds after the click while the browser loads the file;
  // measure the song from when the audio actually runs.
  await expect.poll(() => audioTime(page), { timeout: 10_000 }).toBeGreaterThan(0.1);
  await expect(current).toContainText("Zweite Zeile", { timeout: 4_000 });
  await expect(lyrics.locator('li[data-state="done"]').first()).toContainText("Erste Zeile");
});

test("scrolling away pauses auto-scroll until Back to the song", async ({ page }) => {
  await mockApi(page);
  await page.goto("/songs/test-song");
  await page.getByRole("button", { name: "Play", exact: true }).click();
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
