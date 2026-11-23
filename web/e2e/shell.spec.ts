import { expect, test } from "@playwright/test";

test("navigates between sections and marks the current one", async ({ page }, testInfo) => {
  await page.goto("/");
  await expect(page.getByRole("heading", { level: 1, name: "Welcome back" })).toBeVisible();

  const nav = page.getByRole("navigation", { name: "Main" }).filter({ visible: true });
  await nav.getByRole("link", { name: "Library" }).click();

  await expect(page).toHaveURL(/\/library$/);
  await expect(page.getByRole("heading", { level: 1, name: "Library" })).toBeVisible();
  await expect(nav.getByRole("link", { name: "Library" })).toHaveAttribute("aria-current", "page");

  if (testInfo.project.name === "phone") {
    await expect(nav.getByRole("link", { name: "Profile" })).toBeVisible();
  } else {
    await expect(nav.getByRole("link", { name: "Vocabulary" })).toBeVisible();
  }
});

test("nothing scrolls sideways", async ({ page }) => {
  for (const path of ["/", "/library", "/review", "/vocabulary", "/profile"]) {
    await page.goto(path);
    await expect(page.getByRole("heading", { level: 1 })).toBeVisible();
    const overflow = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
    expect(overflow, `horizontal overflow on ${path}`).toBeLessThanOrEqual(0);
  }
});

test("keyboard: skip link first, then every nav item is reachable", async ({ page }, testInfo) => {
  await page.goto("/");
  await page.keyboard.press("Tab");
  await expect(page.getByRole("link", { name: "Skip to content" })).toBeFocused();
  const nav = page.getByRole("navigation", { name: "Main" }).filter({ visible: true });
  const labels = testInfo.project.name === "phone" ? ["Home", "Library", "Review", "Profile"] : ["Home", "Library", "Review", "Vocabulary"];
  for (const label of labels) {
    const link = nav.getByRole("link", { name: label });
    await link.focus();
    await expect(link).toBeFocused();
    const box = await link.boundingBox();
    expect(box!.height, `${label} tap target`).toBeGreaterThanOrEqual(44);
  }
});

test("the page background is night from the first paint", async ({ page }) => {
  await page.goto("/");
  const bg = await page.evaluate(() => getComputedStyle(document.body).backgroundColor);
  expect(bg).toBe("rgb(18, 8, 31)");
});

test("unknown pages show the 404 with a way home", async ({ page }) => {
  const response = await page.goto("/does-not-exist");
  expect(response?.status()).toBe(404);
  await expect(page.getByRole("heading", { name: "This page isn't playing." })).toBeVisible();
  await page.getByRole("link", { name: "Go home" }).click();
  await expect(page).toHaveURL(/\/$/);
});
