import { defineConfig, devices } from "@playwright/test";

const port = 5173;

export default defineConfig({
  testDir: "./e2e",
  fullyParallel: true,
  // two browsers at a time: like the 4-core CI runners, and stable next to the compose stack locally
  workers: 2,
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
    { name: "phone", use: { ...devices["Desktop Chrome"], viewport: { width: 390, height: 844 } } },
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
