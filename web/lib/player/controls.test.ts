import { describe, expect, it } from "vitest";
import { formatTime, loopRestartMs, nextSpeed, seekForKey, SPEEDS, spokenTime, timeAtPointer } from "./controls";

describe("formatTime", () => {
  it("shows minutes and two-digit seconds", () => {
    expect(formatTime(0)).toBe("0:00");
    expect(formatTime(84.9)).toBe("1:24");
    expect(formatTime(185)).toBe("3:05");
  });

  it("treats an unknown duration as zero", () => {
    expect(formatTime(Number.NaN)).toBe("0:00");
  });
});

describe("spokenTime", () => {
  it("reads naturally for screen readers", () => {
    expect(spokenTime(84)).toBe("1 minute 24 seconds");
    expect(spokenTime(185)).toBe("3 minutes 5 seconds");
    expect(spokenTime(60)).toBe("1 minute");
    expect(spokenTime(1)).toBe("1 second");
    expect(spokenTime(0)).toBe("0 seconds");
  });
});

describe("seekForKey", () => {
  it("arrows seek 5 s, Home and End jump to the ends", () => {
    expect(seekForKey("ArrowRight", 10, 185)).toBe(15);
    expect(seekForKey("ArrowLeft", 10, 185)).toBe(5);
    expect(seekForKey("Home", 90, 185)).toBe(0);
    expect(seekForKey("End", 90, 185)).toBe(185);
  });

  it("stays inside the song", () => {
    expect(seekForKey("ArrowLeft", 2, 185)).toBe(0);
    expect(seekForKey("ArrowRight", 183, 185)).toBe(185);
  });

  it("ignores other keys", () => {
    expect(seekForKey("a", 10, 185)).toBeNull();
  });
});

describe("timeAtPointer", () => {
  const track = { left: 100, width: 400 };
  it("maps the pointer's position along the track to a time", () => {
    expect(timeAtPointer(300, track, 200)).toBe(100);
  });
  it("clamps outside the track", () => {
    expect(timeAtPointer(50, track, 200)).toBe(0);
    expect(timeAtPointer(900, track, 200)).toBe(200);
  });
});

describe("speeds", () => {
  it("are 1, 0.85 and 0.75, and cycle", () => {
    expect(SPEEDS).toEqual([1, 0.85, 0.75]);
    expect(nextSpeed(1)).toBe(0.85);
    expect(nextSpeed(0.85)).toBe(0.75);
    expect(nextSpeed(0.75)).toBe(1);
  });
});

describe("loopRestartMs", () => {
  const lines = [
    { startMs: 1_000, endMs: 3_000 },
    { startMs: 3_000, endMs: 5_000 },
  ];
  it("jumps back to the start once the looped line ends", () => {
    expect(loopRestartMs(lines, 0, 3_000)).toBe(1_000);
    expect(loopRestartMs(lines, 0, 3_050)).toBe(1_000);
  });
  it("does nothing while the line is still playing, or without a loop", () => {
    expect(loopRestartMs(lines, 0, 2_900)).toBeNull();
    expect(loopRestartMs(lines, null, 9_000)).toBeNull();
  });
});
