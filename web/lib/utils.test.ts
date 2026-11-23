import { describe, expect, it } from "vitest";
import { cn } from "./utils";

describe("cn", () => {
  it("keeps a text size and a text colour together", () => {
    expect(cn("text-label", "text-ink")).toBe("text-label text-ink");
  });

  it("the later of two text colours wins", () => {
    expect(cn("text-ink-muted", "text-violet-light")).toBe("text-violet-light");
  });
});
