"use client";

import { motion, useReducedMotion } from "motion/react";
import { duration, ease, springSoft } from "@/lib/motion";
import { cn } from "@/lib/utils";

/*
 * Play ↔ pause as one shape (DESIGN.md → Motion → signature 1). Both icons are two
 * four-point outlines, so every point has a partner and the outline morphs instead of
 * swapping icons: the triangle's left half becomes the left bar, its right half (with
 * its tip doubled) the right bar. Points run top-left, top-right, bottom-right, bottom-left.
 */
const SHAPES = {
  play: ["M7 4.5 L13 8.25 L13 15.75 L7 19.5 Z", "M13 8.25 L19 12 L19 12 L13 15.75 Z"],
  pause: ["M6 5 L10 5 L10 19 L6 19 Z", "M14 5 L18 5 L18 19 L14 19 Z"],
} as const;

export function PlayButton({
  playing,
  onToggle,
  size = "bar",
}: {
  playing: boolean;
  onToggle: () => void;
  /** 64 px in the player bar and sheet, 56 px in the phone mini player. */
  size?: "bar" | "mini";
}) {
  const reduceMotion = useReducedMotion() ?? false;
  const shape = playing ? SHAPES.pause : SHAPES.play;
  // reduced motion: the icon changes at once and only fades, instead of morphing
  const morph = reduceMotion ? { duration: 0 } : springSoft;

  return (
    <motion.button
      type="button"
      onClick={onToggle}
      aria-label={playing ? "Pause" : "Play"}
      whileTap={{ scale: 0.94, transition: { duration: duration.instant, ease: ease.out } }}
      className={cn(
        "relative inline-flex shrink-0 items-center justify-center rounded-pill bg-karaoke text-ink",
        size === "bar" ? "size-16" : "size-14",
      )}
    >
      {/* fades in and out over duration-standard; the inner layer breathes while playing */}
      <span aria-hidden data-on={playing || undefined} className="play-glow pointer-events-none absolute inset-0 rounded-pill">
        <span className="play-glow-breath absolute inset-0 rounded-pill" />
      </span>
      <motion.svg
        key={reduceMotion ? String(playing) : "morph"}
        initial={reduceMotion ? { opacity: 0 } : false}
        animate={{ opacity: 1, transition: { duration: duration.instant } }}
        viewBox="0 0 24 24"
        width={24}
        height={24}
        aria-hidden
        className="relative"
      >
        {shape.map((d, i) => (
          <motion.path key={i} initial={false} animate={{ d }} transition={morph} fill="currentColor" />
        ))}
      </motion.svg>
    </motion.button>
  );
}
