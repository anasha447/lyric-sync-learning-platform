export type TimedLine = { startMs: number; endMs: number };

/**
 * Index of the line being sung at `timeMs`, or -1 when none is (before the first line,
 * in a gap, after the last). A line covers [startMs, endMs). Lines are sorted by start
 * time, so a binary search finds it in O(log n), cheap enough to run on every frame.
 */
export function findLineAt(lines: readonly TimedLine[], timeMs: number): number {
  let low = 0;
  let high = lines.length - 1;
  let candidate = -1;
  // the last line that has started
  while (low <= high) {
    const mid = (low + high) >>> 1;
    if (lines[mid].startMs <= timeMs) {
      candidate = mid;
      low = mid + 1;
    } else {
      high = mid - 1;
    }
  }
  return candidate !== -1 && timeMs < lines[candidate].endMs ? candidate : -1;
}

/** How far through the line we are, from 0 to 1: drives the underline that fills as it's sung. */
export function lineProgress(line: TimedLine, timeMs: number): number {
  const duration = line.endMs - line.startMs;
  if (duration <= 0) return 1;
  return Math.min(1, Math.max(0, (timeMs - line.startMs) / duration));
}
