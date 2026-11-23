import { defineConfig, devices } from "@playwright/test";

const port = 5173;

export default defineConfig({
  testDir: "./e2e",
  fullyParallel: true,
  // two browsers at a time: like the 4-core CI runners, and stable next to the compose stack locally
  workers: 2,
  // a page's first hydration can take over 5 s on a busy machine (the compose stack running next
  // to the browsers); a longer wait only slows failing checks, it never changes what they check
  expect: { timeout: 10_000 },
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? "github" : "list",
  use: {
    baseURL: `http://localhost:${port}`,
    trace: "on-first-retry",
    // Locally use the installed Edge; CI installs Playwright's Chromium.
    channel: process.env.CI ? undefined : "msedge",
  },
  // The two widths every ticket is checked at (DESIGN.md finish checklist).
  projects: [
    // a touch screen, so tests can tap (a tap plays from a line; a mouse click on words does not)
    { name: "phone", use: { ...devices["Desktop Chrome"], viewport: { width: 390, height: 844 }, hasTouch: true } },
    { name: "desktop", use: { ...devices["Desktop Chrome"], viewport: { width: 1440, height: 900 } } },
  ],
  webServer: {
    // CI tests the built Worker (production-like, nothing compiled on the fly); locally the dev server.
    command: process.env.CI || process.env.E2E_TARGET === "preview"
      ? `npm run start -- --port ${port} --strictPort`
      : `npm run dev -- --port ${port} --strictPort`,
    url: `http://localhost:${port}`,
    // a running dev server must not stand in for the preview
    reuseExistingServer: !process.env.CI && process.env.E2E_TARGET !== "preview",
    timeout: 120_000,
  },
});
