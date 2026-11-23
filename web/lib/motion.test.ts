import { describe, expect, it } from "vitest";
import { dampingRatio, duration, pageTransition, springSoft, staggerDelay } from "./motion";

describe("motion tokens", () => {
  it("spring-soft settles without overshoot (No-Bounce rule)", () => {
    expect(dampingRatio(springSoft)).toBeGreaterThanOrEqual(1);
  });

  it("stagger is 30 ms per item and stops growing after 8 items", () => {
    expect(staggerDelay(0)).toBe(0);
    expect(staggerDelay(2)).toBeCloseTo(0.06);
    expect(staggerDelay(7)).toBeCloseTo(0.21);
    expect(staggerDelay(40)).toBeCloseTo(0.21);
  });

  it("pages rise 8 px over duration-slow", () => {
    const t = pageTransition(false);
    expect(t.initial).toEqual({ opacity: 0, y: 8 });
    expect(t.animate.transition.duration).toBe(duration.slow);
  });

  it("with reduced motion pages only crossfade, within 150 ms", () => {
    const t = pageTransition(true);
    expect(t.initial).toEqual({ opacity: 0 });
    expect(t.animate.transition.duration).toBeLessThanOrEqual(0.15);
  });
});
