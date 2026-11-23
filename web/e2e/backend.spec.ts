import { readFileSync } from "node:fs";
import { expect, test } from "@playwright/test";
import { audioTime } from "./fixtures";

/*
 * The happy path against the real backend: no mocks. Needs the compose stack and the seeded
 * song (`node e2e/seed.mjs`), so it only runs with E2E_BACKEND=1:
 *   E2E_BACKEND=1 E2E_TARGET=preview npx playwright test e2e/backend.spec.ts
 */
test.skip(process.env.E2E_BACKEND !== "1", "needs the compose backend: set E2E_BACKEND=1");

const seeded = (): { slug: string; title: string } =>
  JSON.parse(readFileSync(new URL("./.seeded-song.json", import.meta.url), "utf8"));

test("open the library, open a seeded song, and the lyrics light up while it plays", async ({ page }) => {
  const song = seeded();
  await page.goto("/library");
  await page.getByRole("tabpanel").getByRole("link", { name: new RegExp(song.title) }).click();

  await expect(page).toHaveURL(new RegExp(`/songs/${song.slug}$`));
  await expect(page.getByRole("heading", { level: 1, name: song.title })).toBeVisible();

  await page.getByRole("button", { name: "Play", exact: true }).click();
  await expect.poll(() => audioTime(page), { timeout: 15_000 }).toBeGreaterThan(0.6);
  const current = page.getByRole("list", { name: "Lyrics" }).locator('li[aria-current="true"]');
  await expect(current).toContainText("Erste Zeile");
  await expect(current).toContainText("Zweite Zeile", { timeout: 5_000 });
});
