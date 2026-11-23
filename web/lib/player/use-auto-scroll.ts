"use client";

import { animate, useReducedMotion, type AnimationPlaybackControls } from "motion/react";
import { useCallback, useEffect, useRef, useState, type RefObject } from "react";
import { duration, ease } from "@/lib/motion";
import { SCROLL_KEYS, scrollTargetFor } from "./scroll";

/**
 * Keeps the active line at about 40 % of the viewport, gliding over duration-slow.
 * As soon as the learner scrolls themselves (wheel, touch, scroll keys) it lets go and
 * reports `following = false`, so the page can offer "Back to the song". Only real input
 * counts: our own scrolling fires scroll events too, so those can't be used.
 */
export function useAutoScroll(activeIndex: number, activeLineRef: RefObject<HTMLElement | null>) {
  const [following, setFollowing] = useState(true);
  const reduceMotion = useReducedMotion() ?? false;
  const animation = useRef<AnimationPlaybackControls | null>(null);

  const scrollToActive = useCallback(
    (smooth: boolean) => {
      const line = activeLineRef.current;
      if (!line) return;
      const rect = line.getBoundingClientRect();
      const target = scrollTargetFor(rect.top + window.scrollY, rect.height, window.innerHeight);
      animation.current?.stop();
      if (!smooth || reduceMotion) {
        window.scrollTo(0, target);
        return;
      }
      animation.current = animate(window.scrollY, target, {
        duration: duration.slow,
        ease: ease.out,
        onUpdate: (y) => window.scrollTo(0, y),
      });
    },
    [activeLineRef, reduceMotion],
  );

  // a new line starts: follow it, unless the learner is looking elsewhere
  useEffect(() => {
    if (following && activeIndex !== -1) scrollToActive(true);
  }, [activeIndex, following, scrollToActive]);

  useEffect(() => {
    const takeOver = () => {
      animation.current?.stop();
      setFollowing(false);
    };
    const onKey = (event: KeyboardEvent) => {
      const target = event.target as HTMLElement | null;
      const typing = target?.closest("input, textarea, select, button, [contenteditable]");
      if (!typing && SCROLL_KEYS.has(event.key)) takeOver();
    };
    window.addEventListener("wheel", takeOver, { passive: true });
    window.addEventListener("touchmove", takeOver, { passive: true });
    window.addEventListener("keydown", onKey);
    return () => {
      window.removeEventListener("wheel", takeOver);
      window.removeEventListener("touchmove", takeOver);
      window.removeEventListener("keydown", onKey);
      animation.current?.stop();
    };
  }, []);

  const backToSong = useCallback(() => {
    setFollowing(true);
    scrollToActive(true);
  }, [scrollToActive]);

  /** The learner chose a line: follow the song again from there (it scrolls on the next line change). */
  const follow = useCallback(() => setFollowing(true), []);

  return { following, backToSong, follow };
}
