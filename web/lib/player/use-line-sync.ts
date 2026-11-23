"use client";

import { useEffect, useRef, useState, type RefObject } from "react";
import { loopRestartMs } from "./controls";
import { seekAudio } from "./media";
import { findLineAt, lineProgress, type TimedLine } from "./lines";

/**
 * Follows the audio: which line is being sung, and how far through it we are.
 * While playing it reads `currentTime` on every animation frame (smoother than the
 * ~4 Hz `timeupdate` event); while paused it only updates on seek. The progress goes
 * straight into a CSS variable on the active line, so React re-renders only when the
 * line changes, not 60 times a second.
 */
export function useLineSync(
  audio: HTMLAudioElement | null,
  lines: readonly TimedLine[],
  /** The looped line, if any; read every frame, so changing it doesn't restart the loop. */
  loopIndexRef?: RefObject<number | null>,
) {
  const [activeIndex, setActiveIndex] = useState(-1);
  const activeLineRef = useRef<HTMLElement | null>(null);

  useEffect(() => {
    if (!audio) return;
    let frame = 0;
    let current = -1;

    const update = () => {
      let timeMs = audio.currentTime * 1000;
      const restartMs = loopRestartMs(lines, loopIndexRef?.current ?? null, timeMs);
      if (restartMs !== null) {
        seekAudio(audio, restartMs / 1000);
        timeMs = restartMs;
      }
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
    // Animation frames slow down on a busy device and stop in background tabs, but the
    // audio plays on; timeupdate (~4 Hz) keeps the line and the loop correct even then.
    audio.addEventListener("timeupdate", update);
    if (audio.paused) update();
    else start();

    return () => {
      cancelAnimationFrame(frame);
      audio.removeEventListener("play", start);
      audio.removeEventListener("pause", stop);
      audio.removeEventListener("ended", stop);
      audio.removeEventListener("seeked", update);
      audio.removeEventListener("loadedmetadata", update);
      audio.removeEventListener("timeupdate", update);
    };
  }, [audio, lines, loopIndexRef]);

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
