import type { TimedLine } from "./lines";

/** The learning speeds (PROJECT_SPEC F4), always with the pitch preserved. */
export const SPEEDS = [1, 0.85, 0.75] as const;
export type Speed = (typeof SPEEDS)[number];

export function nextSpeed(current: Speed): Speed {
  return SPEEDS[(SPEEDS.indexOf(current) + 1) % SPEEDS.length];
}

/** "1:24": minutes and two-digit seconds, shown in JetBrains Mono so the digits don't jump. */
export function formatTime(seconds: number): string {
  const whole = Number.isFinite(seconds) ? Math.max(0, Math.floor(seconds)) : 0;
  return `${Math.floor(whole / 60)}:${String(whole % 60).padStart(2, "0")}`;
}

/** "1 minute 24 seconds": the timeline's aria-valuetext (DESIGN.md → Wave timeline). */
export function spokenTime(seconds: number): string {
  const whole = Number.isFinite(seconds) ? Math.max(0, Math.floor(seconds)) : 0;
  const minutes = Math.floor(whole / 60);
  const rest = whole % 60;
  const unit = (n: number, word: string) => `${n} ${word}${n === 1 ? "" : "s"}`;
  if (minutes === 0) return unit(rest, "second");
  return rest === 0 ? unit(minutes, "minute") : `${unit(minutes, "minute")} ${unit(rest, "second")}`;
}

const SEEK_STEP_SECONDS = 5;

/** ←/→ seek ±5 s, Home/End jump to the start or end; anything else isn't a seek. */
export function seekForKey(key: string, currentSeconds: number, durationSeconds: number): number | null {
  const clamp = (t: number) => Math.min(durationSeconds, Math.max(0, t));
  switch (key) {
    case "ArrowLeft":
      return clamp(currentSeconds - SEEK_STEP_SECONDS);
    case "ArrowRight":
      return clamp(currentSeconds + SEEK_STEP_SECONDS);
    case "Home":
      return 0;
    case "End":
      return durationSeconds;
    default:
      return null;
  }
}

/** The time under the pointer, from its x position along the timeline. */
export function timeAtPointer(clientX: number, track: { left: number; width: number }, durationSeconds: number): number {
  if (track.width <= 0) return 0;
  const fraction = Math.min(1, Math.max(0, (clientX - track.left) / track.width));
  return fraction * durationSeconds;
}

/** Looping a line: once it has ended, the start time to jump back to; otherwise null. */
export function loopRestartMs(lines: readonly TimedLine[], loopIndex: number | null, timeMs: number): number | null {
  if (loopIndex === null) return null;
  const line = lines[loopIndex];
  return line && timeMs >= line.endMs ? line.startMs : null;
}
