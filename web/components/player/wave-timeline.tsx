"use client";

import { useReducedMotion } from "motion/react";
import { useEffect, useRef, useState, type KeyboardEvent, type PointerEvent } from "react";
import { duration as motionDuration } from "@/lib/motion";
import { formatTime, seekForKey, spokenTime, timeAtPointer } from "@/lib/player/controls";
import { WAVE, wavePath } from "@/lib/player/wave";
import { cn } from "@/lib/utils";

const DRAW_HEIGHT = 12; // 8 px drawing area plus room for the 3 px stroke
const MID = DRAW_HEIGHT / 2;

/** ease-out cubic-bezier(0.22, 1, 0.36, 1) is close to this curve, which is cheap per frame. */
const easeOut = (t: number) => 1 - Math.pow(1 - t, 3);

/**
 * The timeline (DESIGN.md → Motion → signature 3 and Components → Wave timeline): the
 * played part is a flowing sine wave while playing that flattens into a straight line on
 * pause; the rest is a faint straight line. Drawn straight into the SVG on animation
 * frames, and only while something moves.
 */
export function WaveTimeline({
  audio,
  duration,
  playing,
  onSeek,
  size = "bar",
}: {
  audio: HTMLAudioElement | null;
  duration: number;
  playing: boolean;
  onSeek: (seconds: number) => void;
  size?: "bar" | "sheet";
}) {
  const reduceMotion = useReducedMotion() ?? false;
  const trackRef = useRef<HTMLDivElement>(null);
  const playedRef = useRef<SVGPathElement>(null);
  const restRef = useRef<SVGLineElement>(null);
  const thumbRef = useRef<HTMLSpanElement>(null);
  const elapsedRef = useRef<HTMLSpanElement>(null);
  const [width, setWidth] = useState(0);
  const [dragging, setDragging] = useState(false);

  useEffect(() => {
    const track = trackRef.current;
    if (!track) return;
    const observer = new ResizeObserver(([entry]) => setWidth(entry.contentRect.width));
    observer.observe(track);
    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    if (!audio) return;
    let frame = 0;
    let last = performance.now();
    let phase = 0;
    let amplitude = playing && !reduceMotion ? WAVE.amplitude : 0;
    let from = amplitude;
    let to = amplitude;
    let changedAt = last;
    let shownSecond = -1;

    const render = () => {
      const now = performance.now();
      const dt = (now - last) / 1000;
      last = now;
      const target = playing && !reduceMotion ? WAVE.amplitude : 0;
      if (target !== to) {
        from = amplitude;
        to = target;
        changedAt = now;
      }
      const t = Math.min(1, (now - changedAt) / 1000 / motionDuration.slow);
      amplitude = from + (to - from) * easeOut(t);
      if (playing && !reduceMotion) phase = (phase + (dt * WAVE.wavelength) / WAVE.secondsPerWavelength) % WAVE.wavelength;

      const current = audio.currentTime;
      const x = duration > 0 ? Math.min(width, (current / duration) * width) : 0;
      playedRef.current?.setAttribute("d", wavePath(x, MID, amplitude, phase));
      restRef.current?.setAttribute("x1", String(x));
      if (thumbRef.current) thumbRef.current.style.transform = `translateX(${x}px)`;

      const second = Math.floor(current);
      if (second !== shownSecond) {
        shownSecond = second;
        if (elapsedRef.current) elapsedRef.current.textContent = formatTime(current);
        trackRef.current?.setAttribute("aria-valuenow", String(second));
        trackRef.current?.setAttribute("aria-valuetext", `${spokenTime(current)} of ${spokenTime(duration)}`);
      }
      return playing || amplitude !== to || t < 1;
    };

    const loop = () => {
      frame = render() ? requestAnimationFrame(loop) : 0;
    };
    // (re)start drawing: on mount, on play/pause, and when paused but seeked
    const wake = () => {
      if (!frame) {
        last = performance.now();
        frame = requestAnimationFrame(loop);
      }
    };
    wake();
    audio.addEventListener("seeked", wake);
    audio.addEventListener("timeupdate", wake);
    return () => {
      cancelAnimationFrame(frame);
      audio.removeEventListener("seeked", wake);
      audio.removeEventListener("timeupdate", wake);
    };
  }, [audio, duration, playing, reduceMotion, width]);

  const seekToPointer = (event: PointerEvent<HTMLDivElement>) => {
    const rect = event.currentTarget.getBoundingClientRect();
    onSeek(timeAtPointer(event.clientX, rect, duration));
  };

  const onKeyDown = (event: KeyboardEvent<HTMLDivElement>) => {
    const target = seekForKey(event.key, audio?.currentTime ?? 0, duration);
    if (target === null) return;
    event.preventDefault();
    onSeek(target);
  };

  return (
    <div className="flex w-full min-w-0 items-center gap-3">
      <span ref={elapsedRef} className="w-10 shrink-0 text-right font-mono text-mono text-ink">
        0:00
      </span>
      <div
        ref={trackRef}
        role="slider"
        tabIndex={0}
        aria-label="Song position"
        aria-valuemin={0}
        aria-valuemax={Math.floor(duration)}
        aria-valuenow={0}
        aria-valuetext={`0 seconds of ${spokenTime(duration)}`}
        data-dragging={dragging || undefined}
        onPointerDown={(event) => {
          event.currentTarget.setPointerCapture(event.pointerId);
          setDragging(true);
          seekToPointer(event);
        }}
        onPointerMove={(event) => dragging && seekToPointer(event)}
        onPointerUp={() => setDragging(false)}
        onPointerCancel={() => setDragging(false)}
        onKeyDown={onKeyDown}
        className={cn(
          "group relative flex min-w-0 flex-1 cursor-pointer touch-none items-center rounded-sm",
          size === "sheet" ? "h-11" : "h-8",
        )}
      >
        <svg width="100%" height={DRAW_HEIGHT} className="overflow-visible" aria-hidden>
          <line ref={restRef} x1={0} x2={width} y1={MID} y2={MID} className="stroke-ink/20" strokeWidth={2} strokeLinecap="round" />
          <path ref={playedRef} fill="none" className="stroke-karaoke" strokeWidth={3} strokeLinecap="round" strokeLinejoin="round" />
        </svg>
        <span
          ref={thumbRef}
          aria-hidden
          className="pointer-events-none absolute top-1/2 left-0 -mt-1.5 -ml-1.5 size-3 rounded-pill bg-ink opacity-0 transition-opacity duration-(--duration-instant) group-hover:opacity-100 group-focus-visible:opacity-100 group-data-dragging:opacity-100"
        />
      </div>
      <span className="w-10 shrink-0 font-mono text-mono text-ink-muted">{formatTime(duration)}</span>
    </div>
  );
}
