import { expect, test } from "@playwright/test";
import { audioTime, mockApi, openControls, testSong } from "./fixtures";

test.beforeEach(async ({ page }) => {
  await mockApi(page);
  await page.goto("/songs/test-song");
  await expect(page.getByRole("heading", { level: 1, name: "Das Testlied" })).toBeVisible();
});

test("play, pause and the space bar", async ({ page }) => {
  const play = page.getByRole("button", { name: "Play", exact: true });
  await play.click();
  await expect(page.getByRole("button", { name: "Pause", exact: true })).toBeVisible();
  await expect.poll(() => audioTime(page), { timeout: 10_000 }).toBeGreaterThan(0.1);

  // Space toggles anywhere on the page, but not while a control has focus
  await page.locator("h1").click();
  await page.keyboard.press("Space");
  await expect(play).toBeVisible();
  expect(await page.evaluate(() => document.querySelector("audio")!.paused)).toBe(true);
});

test("the timeline seeks with the keyboard and tells screen readers where it is", async ({ page }, testInfo) => {
  const controls = await openControls(page, testInfo);
  const slider = controls.getByRole("slider", { name: "Song position" });
  await slider.focus();
  await expect(slider).toBeFocused();

  await page.keyboard.press("ArrowRight");
  await expect.poll(() => audioTime(page)).toBeCloseTo(5, 0);
  await expect(slider).toHaveAttribute("aria-valuetext", /^5 seconds of 6 seconds$/);

  await page.keyboard.press("Home");
  await expect.poll(() => audioTime(page)).toBe(0);
  await expect(slider).toHaveAttribute("aria-valuetext", /^0 seconds of/);
});

test("clicking the timeline seeks", async ({ page }, testInfo) => {
  const controls = await openControls(page, testInfo);
  const box = (await controls.getByRole("slider", { name: "Song position" }).boundingBox())!;
  await page.mouse.click(box.x + box.width / 2, box.y + box.height / 2);
  await expect.poll(() => audioTime(page)).toBeCloseTo(3, 0);
});

test("speed cycles 1, 0.85, 0.75 with the pitch kept", async ({ page }, testInfo) => {
  const controls = await openControls(page, testInfo);
  const speed = controls.getByRole("button", { name: /^Speed/ });
  await expect(speed).toHaveText("1×");
  await speed.click();
  await expect(speed).toHaveText("0.85×");
  expect(await page.evaluate(() => [document.querySelector("audio")!.playbackRate, document.querySelector("audio")!.preservesPitch])).toEqual([0.85, true]);
  await speed.click();
  await expect(speed).toHaveText("0.75×");
  await speed.click();
  await expect(speed).toHaveText("1×");
});

test("translations can be hidden", async ({ page }, testInfo) => {
  const lyrics = page.getByRole("list", { name: "Lyrics" });
  await expect(lyrics.getByText("First line")).toBeVisible();
  const controls = await openControls(page, testInfo);
  const toggle = controls.getByRole("button", { name: "Show translations" });
  await expect(toggle).toHaveAttribute("aria-pressed", "true");
  await toggle.click();
  await expect(toggle).toHaveAttribute("aria-pressed", "false");
  await expect(lyrics.getByText("First line")).toHaveCount(0);
});

test("looping a line plays it again instead of moving on", async ({ page }, testInfo) => {
  await page.getByRole("button", { name: "Play", exact: true }).click();
  await expect.poll(() => audioTime(page), { timeout: 10_000 }).toBeGreaterThan(0.1);
  const controls = await openControls(page, testInfo);
  const loop = controls.getByRole("button", { name: "Loop this line" });
  await loop.click();
  await expect(loop).toHaveAttribute("aria-pressed", "true");

  // whichever line was playing when Loop was pressed: the audio must stay inside it
  const looped = page.getByRole("list", { name: "Lyrics" }).getByRole("listitem").filter({ hasText: "(looping)" });
  await expect(looped).toHaveCount(1);
  const index = await looped.evaluate((li) => Array.from(li.parentElement!.children).indexOf(li));
  const line = testSong.lines[index];
  // the jump back happens on the next frame or timeupdate after the end, so allow a little overshoot
  for (let i = 0; i < 6; i++) {
    await page.waitForTimeout(500);
    const t = await audioTime(page);
    expect(t).toBeGreaterThanOrEqual(line.startMs / 1000 - 0.05);
    expect(t).toBeLessThan(line.endMs / 1000 + 0.3);
  }
});

test("phone: the mini player opens a sheet that Escape closes", async ({ page }, testInfo) => {
  test.skip(testInfo.project.name !== "phone", "the sheet is the phone layout");
  const opener = page.getByRole("button", { name: /Open the player/ });
  await opener.click();
  const sheet = page.getByRole("dialog", { name: "Player" });
  await expect(sheet).toBeVisible();
  await expect(sheet.getByRole("button", { name: "Close the player" })).toBeFocused();
  await page.keyboard.press("Escape");
  await expect(sheet).toHaveCount(0);
  await expect(opener).toBeFocused();
});
