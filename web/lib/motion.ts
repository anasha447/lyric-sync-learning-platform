import type { Transition } from "motion/react";

/**
 * Motion tokens from DESIGN.md → Motion. Components use these, never their own timings.
 * Durations are in seconds, as Motion expects.
 */
export const duration = {
  instant: 0.15,
  standard: 0.28,
  slow: 0.45,
  ambient: 0.6,
  /** Leaving pages fade out a little faster than they arrive. */
  exit: 0.2,
} as const;

export const ease = {
  out: [0.22, 1, 0.36, 1],
  in: [0.4, 0, 1, 1],
} as const satisfies Record<string, [number, number, number, number]>;

/** Critically damped (ratio ≈ 1.01): settles without overshoot, per the No-Bounce rule. */
export const springSoft = { type: "spring", stiffness: 300, damping: 35, mass: 1 } as const satisfies Transition;

export const stagger = { each: 0.03, maxItems: 8 } as const;

/** Damping ratio ζ = c / (2·√(k·m)). Below 1 a spring overshoots. */
export function dampingRatio(spring: { stiffness: number; damping: number; mass: number }): number {
  return spring.damping / (2 * Math.sqrt(spring.stiffness * spring.mass));
}

/** Delay for the n-th item of a list, capped so long lists don't keep the learner waiting. */
export function staggerDelay(index: number): number {
  return Math.min(index, stagger.maxItems - 1) * stagger.each;
}

/**
 * Page enter/exit. With reduced motion every movement becomes a crossfade of at most 150 ms.
 */
export function pageTransition(reduceMotion: boolean) {
  if (reduceMotion) {
    const fade = { duration: duration.instant, ease: ease.out };
    return {
      initial: { opacity: 0 },
      animate: { opacity: 1, transition: fade },
      exit: { opacity: 0, transition: fade },
    };
  }
  return {
    initial: { opacity: 0, y: 8 },
    animate: { opacity: 1, y: 0, transition: { duration: duration.slow, ease: ease.out } },
    exit: { opacity: 0, transition: { duration: duration.exit, ease: ease.in } },
  };
}
