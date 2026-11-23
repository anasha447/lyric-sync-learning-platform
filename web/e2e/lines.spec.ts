import { expect, test, type Page } from "@playwright/test";
import { audioTime, mockApi, openControls } from "./fixtures";

// DESIGN.md → Components → Lyric line and Mini player (FE-3.7)

const lyrics = (page: Page) => page.getByRole("list", { name: "Lyrics" });
const lineItem = (page: Page, text: string) => lyrics(page).getByRole("listitem").filter({ hasText: text });
const isPaused = (page: Page) => page.evaluate(() => document.querySelector("audio")!.paused);

test.beforeEach(async ({ page }) => {
  await mockApi(page);
  await page.goto("/songs/test-song");
  await expect(page.getByRole("heading", { level: 1, name: "Das Testlied" })).toBeVisible();
});

test("the Play from button jumps to the line's start and plays", async ({ page }) => {
  await page.getByRole("button", { name: "Play from: Zweite Zeile" }).focus();
  await page.keyboard.press("Enter");
  await expect.poll(() => audioTime(page), { timeout: 10_000 }).toBeGreaterThanOrEqual(1.5);
  expect(await isPaused(page)).toBe(false);
  await expect(lineItem(page, "Zweite Zeile")).toHaveAttribute("aria-current", "true");
});

test("phone: tapping anywhere on a line, words included, plays from it", async ({ page }, testInfo) => {
  test.skip(testInfo.project.name !== "phone", "touch");
  await lineItem(page, "Zweite Zeile").getByText("Zweite Zeile").tap();
  await expect.poll(() => audioTime(page), { timeout: 10_000 }).toBeGreaterThanOrEqual(1.5);
  expect(await audioTime(page)).toBeLessThan(3);
  expect(await isPaused(page)).toBe(false);
});

test("PC: a click on the words waits for the word card; the translation and ▶ play from the line", async ({ page }, testInfo) => {
  test.skip(testInfo.project.name !== "desktop", "mouse");
  await lineItem(page, "Zweite Zeile").getByText("Zweite Zeile").click();
  await page.waitForTimeout(300);
  expect(await isPaused(page)).toBe(true);
  expect(await audioTime(page)).toBe(0);

  await lineItem(page, "Zweite Zeile").getByText("Second line").click();
  await expect.poll(() => audioTime(page), { timeout: 10_000 }).toBeGreaterThanOrEqual(1.5);

  const last = lineItem(page, "Letzte Zeile");
  await last.hover();
  const play = last.getByRole("button", { name: "Play from: Letzte Zeile" });
  await expect(play).toBeVisible();
  await play.click();
  await expect.poll(() => audioTime(page)).toBeGreaterThanOrEqual(4.5);
});

test("with Loop on, the loop moves to the line you jump to", async ({ page }, testInfo) => {
  await page.getByRole("button", { name: "Play", exact: true }).click();
  await expect.poll(() => audioTime(page), { timeout: 10_000 }).toBeGreaterThan(0.1);
  const controls = await openControls(page, testInfo);
  await controls.getByRole("button", { name: "Loop this line" }).click();
  if (testInfo.project.name === "phone") await page.keyboard.press("Escape");

  await page.getByRole("button", { name: "Play from: Zweite Zeile" }).focus();
  await page.keyboard.press("Enter");
  await expect(lineItem(page, "Zweite Zeile")).toContainText("(looping)");
  await expect(lyrics(page).getByText("(looping)")).toHaveCount(1);
  for (let i = 0; i < 5; i++) {
    await page.waitForTimeout(500);
    const t = await audioTime(page);
    expect(t).toBeGreaterThanOrEqual(1.45);
    expect(t).toBeLessThan(3.3);
  }
});

test("scrolling never changes the song's time", async ({ page }) => {
  await page.mouse.move(200, 400);
  await page.mouse.wheel(0, 300);
  await page.waitForTimeout(400);
  expect(await audioTime(page)).toBe(0);
  expect(await isPaused(page)).toBe(true);
});

test("phone: the mini player shows the timeline, and tapping it seeks", async ({ page }, testInfo) => {
  test.skip(testInfo.project.name !== "phone", "the mini player is the phone layout");
  const slider = page.getByRole("region", { name: "Player" }).getByRole("slider", { name: "Song position" });
  await expect(slider).toBeVisible();
  const box = (await slider.boundingBox())!;
  expect(box.height).toBeGreaterThanOrEqual(44);
  await page.touchscreen.tap(box.x + box.width * 0.75, box.y + box.height - 6);
  await expect.poll(() => audioTime(page)).toBeCloseTo(4.5, 0);
});

test("phone: with translations off, tapping a line's words shows its translation", async ({ page }, testInfo) => {
  test.skip(testInfo.project.name !== "phone", "touch");
  const controls = await openControls(page, testInfo);
  await controls.getByRole("button", { name: "Show translations" }).click();
  await page.keyboard.press("Escape");
  await expect(lyrics(page).getByText("Second line")).toHaveCount(0);

  await lineItem(page, "Zweite Zeile").getByText("Zweite Zeile").tap();
  await expect(lyrics(page).getByText("Second line")).toBeVisible();
  await expect(lyrics(page).getByText("First line")).toHaveCount(0);
});
