import { describe, expect, it } from "vitest";
import { ARTWORK_COLOURS, ARTWORK_SHAPES, artworkFor } from "./artwork";

const slugs = Array.from({ length: 60 }, (_, i) => `song-${i}-${(i * 7919) % 101}`);

describe("artworkFor", () => {
  it("uses DESIGN.md's palette and six shapes", () => {
    expect(ARTWORK_COLOURS).toEqual(["violet", "karaoke", "plum", "teal", "amber"]);
    expect(ARTWORK_SHAPES).toEqual(["circle", "arc", "wave", "bloom", "stripe", "orbit"]);
  });

  it("is the same every time for the same song", () => {
    expect(artworkFor("der-lindenbaum")).toEqual(artworkFor("der-lindenbaum"));
  });

  it("picks two different palette colours, an angle and a shape", () => {
    for (const slug of slugs) {
      const art = artworkFor(slug);
      expect(ARTWORK_COLOURS).toContain(art.from);
      expect(ARTWORK_COLOURS).toContain(art.to);
      expect(art.from).not.toBe(art.to);
      expect(ARTWORK_SHAPES).toContain(art.shape);
      expect(art.angle).toBeGreaterThanOrEqual(0);
      expect(art.angle).toBeLessThan(360);
    }
  });

  it("different songs look different", () => {
    const shapes = new Set(slugs.map((s) => artworkFor(s).shape));
    const pairs = new Set(slugs.map((s) => `${artworkFor(s).from}/${artworkFor(s).to}`));
    expect(shapes.size).toBe(6);
    expect(pairs.size).toBeGreaterThanOrEqual(10);
  });
});
