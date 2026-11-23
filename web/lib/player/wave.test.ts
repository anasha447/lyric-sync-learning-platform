import { describe, expect, it } from "vitest";
import { WAVE, wavePath } from "./wave";

const points = (d: string) =>
  [...d.matchAll(/-?\d+(?:\.\d+)?/g)].map(Number).reduce<number[][]>((acc, n, i) => {
    if (i % 2 === 0) acc.push([n]);
    else acc[acc.length - 1].push(n);
    return acc;
  }, []);

describe("wavePath", () => {
  it("uses the DESIGN.md wave: 24 px wavelength, 3 px amplitude", () => {
    expect(WAVE).toEqual({ wavelength: 24, amplitude: 3, secondsPerWavelength: 1.2 });
  });

  it("is a straight line at amplitude 0 (paused)", () => {
    const ys = points(wavePath(100, 4, 0, 0)).map(([, y]) => y);
    expect(new Set(ys)).toEqual(new Set([4]));
  });

  it("swings 3 px either side of the middle while playing", () => {
    const ys = points(wavePath(240, 4, 3, 0)).map(([, y]) => y);
    expect(Math.min(...ys)).toBeCloseTo(1, 1);
    expect(Math.max(...ys)).toBeCloseTo(7, 1);
  });

  it("starts at x = 0 and ends exactly at the given width", () => {
    const xs = points(wavePath(97, 4, 3, 0)).map(([x]) => x);
    expect(xs[0]).toBe(0);
    expect(xs.at(-1)).toBe(97);
  });

  it("draws nothing for a zero width (nothing played yet)", () => {
    expect(wavePath(0, 4, 3, 0)).toBe("");
  });
});
