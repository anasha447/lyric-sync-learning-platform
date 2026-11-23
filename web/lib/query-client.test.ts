import { describe, expect, it } from "vitest";
import { makeQueryClient, songKeys } from "./query-client";

describe("query client", () => {
  it("published songs never go stale within a session (ADR-0011)", () => {
    const client = makeQueryClient();
    expect(client.getQueryDefaults(songKeys.detail("am-brunnen")).staleTime).toBe(Infinity);
    expect(client.getQueryDefaults(songKeys.page("A2", 0)).staleTime).toBe(Infinity);
  });

  it("other queries use the short default", () => {
    const client = makeQueryClient();
    expect(client.getQueryDefaults(["reviews", "due"]).staleTime).toBeUndefined();
    expect(client.getDefaultOptions().queries?.staleTime).toBe(30_000);
  });
});
