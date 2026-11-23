"use client";

import { useEffect, useRef, useState } from "react";
import { findLineAt, lineProgress, type TimedLine } from "./lines";

/**
 * Follows the audio: which line is being sung, and how far through it we are.
 * While playing it reads `currentTime` on every animation frame (smoother than the
 * ~4 Hz `timeupdate` event); while paused it only updates on seek. The progress goes
 * straight into a CSS variable on the active line, so React re-renders only when the
 * line changes, not 60 times a second.
 */
export function useLineSync(audio: HTMLAudioElement | null, lines: readonly TimedLine[]) {
  const [activeIndex, setActiveIndex] = useState(-1);
  const activeLineRef = useRef<HTMLElement | null>(null);

  useEffect(() => {
    if (!audio) return;
    let frame = 0;
    let current = -1;

    const update = () => {
      const timeMs = audio.currentTime * 1000;
      const index = findLineAt(lines, timeMs);
      if (index !== current) {
        current = index;
        setActiveIndex(index);
      }
      if (index !== -1) {
        activeLineRef.current?.style.setProperty("--line-progress", String(lineProgress(lines[index], timeMs)));
      }
    };
    const loop = () => {
      update();
      frame = requestAnimationFrame(loop);
    };
    const start = () => {
      cancelAnimationFrame(frame);
      frame = requestAnimationFrame(loop);
    };
    const stop = () => {
      cancelAnimationFrame(frame);
      update();
    };

    audio.addEventListener("play", start);
    audio.addEventListener("pause", stop);
    audio.addEventListener("ended", stop);
    audio.addEventListener("seeked", update);
    audio.addEventListener("loadedmetadata", update);
    if (audio.paused) update();
    else start();

    return () => {
      cancelAnimationFrame(frame);
      audio.removeEventListener("play", start);
      audio.removeEventListener("pause", stop);
      audio.removeEventListener("ended", stop);
      audio.removeEventListener("seeked", update);
      audio.removeEventListener("loadedmetadata", update);
    };
  }, [audio, lines]);

  return { activeIndex, activeLineRef };
}

/** Whether the audio is playing, kept in sync with the element's own events. */
export function usePlaying(audio: HTMLAudioElement | null): boolean {
  const [playing, setPlaying] = useState(false);
  useEffect(() => {
    if (!audio) return;
    const sync = () => setPlaying(!audio.paused);
    sync();
    audio.addEventListener("play", sync);
    audio.addEventListener("pause", sync);
    audio.addEventListener("ended", sync);
    return () => {
      audio.removeEventListener("play", sync);
      audio.removeEventListener("pause", sync);
      audio.removeEventListener("ended", sync);
    };
  }, [audio]);
  return playing;
}
