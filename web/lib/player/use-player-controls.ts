"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { nextSpeed, type Speed } from "./controls";
import type { TimedLine } from "./lines";
import { seekAudio, setAudioSpeed } from "./media";
import { usePlaying } from "./use-line-sync";

/**
 * Everything the player's controls change on the one <audio> element: play/pause, seek,
 * speed (pitch preserved), looping a line, and whether translations show.
 */
export function usePlayerControls(audio: HTMLAudioElement | null, lines: readonly TimedLine[], fallbackDurationMs: number) {
  const playing = usePlaying(audio);
  const [speed, setSpeed] = useState<Speed>(1);
  const [showTranslations, setShowTranslations] = useState(true);
  const [loopIndex, setLoopIndex] = useState<number | null>(null);
  const loopIndexRef = useRef<number | null>(null);
  const [duration, setDuration] = useState(fallbackDurationMs / 1000);

  useEffect(() => {
    loopIndexRef.current = loopIndex;
  }, [loopIndex]);

  // the file's real length once its metadata is in; the API's value until then
  useEffect(() => {
    if (!audio) return;
    const sync = () => {
      if (Number.isFinite(audio.duration) && audio.duration > 0) setDuration(audio.duration);
    };
    sync();
    audio.addEventListener("durationchange", sync);
    return () => audio.removeEventListener("durationchange", sync);
  }, [audio]);

  const toggle = useCallback(() => {
    if (!audio) return;
    if (audio.paused) void audio.play();
    else audio.pause();
  }, [audio]);

  const seekTo = useCallback(
    (seconds: number) => {
      if (!audio) return;
      seekAudio(audio, seconds);
      // seeking away from the looped line ends the loop
      const looped = loopIndexRef.current === null ? null : lines[loopIndexRef.current];
      if (looped && (seconds * 1000 < looped.startMs || seconds * 1000 >= looped.endMs)) setLoopIndex(null);
    },
    [audio, lines],
  );

  const cycleSpeed = useCallback(() => {
    setSpeed((current) => {
      const next = nextSpeed(current);
      if (audio) setAudioSpeed(audio, next);
      return next;
    });
  }, [audio]);

  /** Loops `lineIndex` (the line being sung, or the last one sung), or stops looping. */
  const toggleLoop = useCallback((lineIndex: number) => {
    setLoopIndex((current) => (current === null && lineIndex !== -1 ? lineIndex : null));
  }, []);

  const toggleTranslations = useCallback(() => setShowTranslations((s) => !s), []);

  return {
    playing,
    toggle,
    seekTo,
    duration,
    speed,
    cycleSpeed,
    loopIndex,
    loopIndexRef,
    toggleLoop,
    showTranslations,
    toggleTranslations,
  };
}

export type PlayerControls = ReturnType<typeof usePlayerControls>;

/** True when the media query matches; false on the server and before the first effect. */
export function useMediaQuery(query: string): boolean {
  const [matches, setMatches] = useState(false);
  useEffect(() => {
    const list = window.matchMedia(query);
    const sync = () => setMatches(list.matches);
    sync();
    list.addEventListener("change", sync);
    return () => list.removeEventListener("change", sync);
  }, [query]);
  return matches;
}
