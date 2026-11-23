import { describe, expect, it } from "vitest";
import { scrollTargetFor } from "./scroll";

describe("scrollTargetFor", () => {
  it("puts the middle of the line at 40 % of the viewport", () => {
    // line centre at 1 000 + 20 = 1 020; 40 % of 800 = 320
    expect(scrollTargetFor(1_000, 40, 800)).toBe(700);
  });

  it("never scrolls above the top of the page", () => {
    expect(scrollTargetFor(100, 40, 800)).toBe(0);
  });
});
