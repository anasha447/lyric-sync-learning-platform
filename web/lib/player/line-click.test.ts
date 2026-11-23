import { describe, expect, it } from "vitest";
import { lineClickPlays, lineClickRevealsTranslation } from "./line-click";

// DESIGN.md → Components → Lyric line: time vs. meaning, per device
describe("lineClickPlays", () => {
  it("a touch plays from the line anywhere, words included", () => {
    expect(lineClickPlays("touch", true)).toBe(true);
    expect(lineClickPlays("touch", false)).toBe(true);
    expect(lineClickPlays("pen", true)).toBe(true);
  });

  it("a mouse plays from the empty space and the translation, not from a word", () => {
    expect(lineClickPlays("mouse", false)).toBe(true);
    expect(lineClickPlays("mouse", true)).toBe(false);
  });

  it("an unknown pointer counts as a mouse", () => {
    expect(lineClickPlays(undefined, true)).toBe(false);
    expect(lineClickPlays("", false)).toBe(true);
  });
});

describe("lineClickRevealsTranslation", () => {
  it("only a touch on the words shows the line's hidden translation", () => {
    expect(lineClickRevealsTranslation("touch", true)).toBe(true);
    expect(lineClickRevealsTranslation("touch", false)).toBe(false);
    expect(lineClickRevealsTranslation("mouse", false)).toBe(false);
  });
});
