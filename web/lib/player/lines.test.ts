import { describe, expect, it } from "vitest";
import { findLineAt, lineProgress, type TimedLine } from "./lines";

// 1 s gap between the second and third line, like a short instrumental break.
const lines: TimedLine[] = [
  { startMs: 1_000, endMs: 3_000 },
  { startMs: 3_000, endMs: 5_000 },
  { startMs: 6_000, endMs: 8_000 },
];

describe("findLineAt", () => {
  it("before the first line nothing is sung", () => {
    expect(findLineAt(lines, 0)).toBe(-1);
    expect(findLineAt(lines, 999)).toBe(-1);
  });

  it("a line starts exactly at its start time", () => {
    expect(findLineAt(lines, 1_000)).toBe(0);
    expect(findLineAt(lines, 6_000)).toBe(2);
  });

  it("the end time belongs to the next line, not this one", () => {
    expect(findLineAt(lines, 2_999)).toBe(0);
    expect(findLineAt(lines, 3_000)).toBe(1);
  });

  it("between lines nothing is sung", () => {
    expect(findLineAt(lines, 5_000)).toBe(-1);
    expect(findLineAt(lines, 5_500)).toBe(-1);
  });

  it("after the last line nothing is sung", () => {
    expect(findLineAt(lines, 8_000)).toBe(-1);
    expect(findLineAt(lines, 120_000)).toBe(-1);
  });

  it("a song without lines never has an active line", () => {
    expect(findLineAt([], 1_000)).toBe(-1);
  });

  it("finds the right line in a long song", () => {
    const many = Array.from({ length: 500 }, (_, i) => ({ startMs: i * 2_000, endMs: i * 2_000 + 1_500 }));
    expect(findLineAt(many, 777 * 1_000 + 200)).toBe(388);
    // line 388 runs 776.0–777.5 s, line 389 starts at 778.0 s
    expect(findLineAt(many, 777_600)).toBe(-1);
  });
});

describe("lineProgress", () => {
  it("runs from 0 at the start to 1 at the end", () => {
    expect(lineProgress(lines[0], 1_000)).toBe(0);
    expect(lineProgress(lines[0], 2_000)).toBe(0.5);
    expect(lineProgress(lines[0], 3_000)).toBe(1);
  });

  it("stays between 0 and 1 outside the line", () => {
    expect(lineProgress(lines[0], 0)).toBe(0);
    expect(lineProgress(lines[0], 9_000)).toBe(1);
  });
});
