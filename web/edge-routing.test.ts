import { describe, expect, it } from "vitest";
import { isBackendPath } from "./edge-routing";

describe("isBackendPath", () => {
  it.each(["/api", "/api/songs", "/api/songs?level=A2", "/media/audio/x.mp3"])("%s goes to the backend", (url) => {
    expect(isBackendPath(url)).toBe(true);
  });

  it.each(["/", "/library", "/songs/am-brunnen", "/apis", "/mediathek", "/_next/static/x.js"])("%s stays with the Worker", (url) => {
    expect(isBackendPath(url)).toBe(false);
  });
});
