"use client";

import { AnimatePresence, motion } from "motion/react";
import { useEffect, useState } from "react";
import { duration, ease } from "@/lib/motion";
import { useAutoScroll } from "@/lib/player/use-auto-scroll";
import { useLineSync } from "@/lib/player/use-line-sync";
import { usePlayerControls } from "@/lib/player/use-player-controls";
import type { PublicSong } from "@/lib/songs";
import { LineView, type LineState } from "./line-view";
import { Player } from "./player-bar";

/**
 * The song's lines, lit up in time with the one <audio> element, plus the player that
 * controls it: the bar on tablet and desktop, the mini player and sheet on phones.
 */
export function LyricsPlayer({ song }: { song: PublicSong }) {
  const [audio, setAudio] = useState<HTMLAudioElement | null>(null);
  const controls = usePlayerControls(audio, song.lines, song.durationMs);
  const { activeIndex, activeLineRef } = useLineSync(audio, song.lines, controls.loopIndexRef);
  const { following, backToSong } = useAutoScroll(activeIndex, activeLineRef);
  // the last line that started, kept through gaps; going back in the song lowers it again
  const [lastSung, setLastSung] = useState(-1);
  if (activeIndex !== -1 && activeIndex !== lastSung) setLastSung(activeIndex);
  const currentLine = activeIndex !== -1 ? activeIndex : lastSung;

  const stateOf = (index: number): LineState =>
    index === activeIndex ? "active" : index <= lastSung ? "done" : "upcoming";

  // Space plays and pauses anywhere on the song page, unless a control has focus
  const { toggle } = controls;
  useEffect(() => {
    const onKey = (event: KeyboardEvent) => {
      if (event.key !== " " || event.repeat) return;
      const target = event.target as HTMLElement | null;
      if (target?.closest("button, a, input, textarea, select, [role='slider'], [contenteditable]")) return;
      event.preventDefault();
      toggle();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [toggle]);

  return (
    <div className="flex flex-col items-center">
      <audio ref={setAudio} src={song.audioUrl} preload="metadata" />

      <ol lang="de" aria-label="Lyrics" className="flex w-full max-w-(--width-lyrics) flex-col">
        {song.lines.map((line, i) => (
          <LineView
            key={line.index}
            line={line}
            state={stateOf(i)}
            looped={controls.loopIndex === i}
            showTranslation={controls.showTranslations}
            ref={i === activeIndex ? (el) => void (activeLineRef.current = el) : undefined}
          />
        ))}
      </ol>

      {/* room under the last line, so the player never covers it */}
      <div aria-hidden className="h-[calc(64px+24px)] md:h-[calc(var(--height-player-bar)+24px)]" />

      <Player song={song} audio={audio} controls={controls} currentLine={currentLine} />

      <AnimatePresence>
        {!following && controls.playing && activeIndex !== -1 && (
          <motion.button
            type="button"
            onClick={backToSong}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0, transition: { duration: duration.standard, ease: ease.out } }}
            exit={{ opacity: 0, transition: { duration: duration.exit, ease: ease.in } }}
            className="fixed bottom-[calc(var(--height-tab-bar)+64px+24px)] left-1/2 z-20 -translate-x-1/2 rounded-pill bg-plum px-6 py-3 text-label text-ink shadow-overlay md:bottom-[calc(var(--height-player-bar)+16px)] md:left-[calc(50%+var(--width-sidebar-collapsed)/2)] lg:left-[calc(50%+var(--width-sidebar)/2)]"
          >
            Back to the song
          </motion.button>
        )}
      </AnimatePresence>
    </div>
  );
}
