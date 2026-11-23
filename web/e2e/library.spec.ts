import { expect, test, type Page } from "@playwright/test";

const summary = (slug: string, title: string, level: string) => ({ slug, title, artist: "Someone Famous", level, durationMs: 180_000 });
const songs = [
  summary("am-brunnen", "Am Brunnen vor dem Tore", "A2"),
  summary("die-gedanken", "Die Gedanken sind frei", "B1"),
  summary("kuckuck", "Kuckuck, Kuckuck, ruft's aus dem Wald", "A1"),
];

async function mockList(page: Page) {
  await page.route(/\/api\/songs\?/, (route) => {
    const level = new URL(route.request().url()).searchParams.get("level");
    const items = level ? songs.filter((s) => s.level === level) : songs;
    return route.fulfill({ json: { items, page: 0, size: 20, totalItems: items.length, totalPages: 1 } });
  });
}

test("the library shows song cards that open the song", async ({ page }) => {
  await mockList(page);
  await page.goto("/library");
  const grid = page.getByRole("tabpanel");
  const card = grid.getByRole("link", { name: /Am Brunnen vor dem Tore/ });
  await expect(card).toBeVisible();
  await expect(grid.getByRole("link")).toHaveCount(3);
  // cards show the level but never the artist (DESIGN.md → Song card)
  await expect(card).toContainText("A2");
  await expect(grid).not.toContainText("Someone Famous");
  await expect(card).toHaveAttribute("href", "/songs/am-brunnen");
});

test("level tabs filter the songs and work with arrow keys", async ({ page }) => {
  await mockList(page);
  await page.goto("/library");
  const tabs = page.getByRole("tablist", { name: "Level" });
  await expect(tabs.getByRole("tab", { name: "All levels" })).toHaveAttribute("aria-selected", "true");

  await tabs.getByRole("tab", { name: "B1" }).click();
  const grid = page.getByRole("tabpanel");
  await expect(grid.getByRole("link")).toHaveCount(1);
  await expect(grid).toContainText("Die Gedanken sind frei");

  await page.keyboard.press("ArrowRight");
  await expect(tabs.getByRole("tab", { name: "B2" })).toBeFocused();
  await expect(tabs.getByRole("tab", { name: "B2" })).toHaveAttribute("aria-selected", "true");
  await expect(grid).toContainText("No B2 songs yet.");
  await grid.getByRole("button", { name: "Show all levels" }).click();
  await expect(grid.getByRole("link")).toHaveCount(3);
});

test("/songs leads to the library", async ({ page }) => {
  await mockList(page);
  await page.goto("/songs");
  await expect(page).toHaveURL(/\/library$/);
  await expect(page.getByRole("heading", { level: 1, name: "Library" })).toBeVisible();
});
