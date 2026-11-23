"use client";

import { Pause, Play } from "lucide-react";
import { AnimatePresence, motion } from "motion/react";
import { useState } from "react";
import { duration, ease } from "@/lib/motion";
import { useAutoScroll } from "@/lib/player/use-auto-scroll";
import { useLineSync, usePlaying } from "@/lib/player/use-line-sync";
import type { PublicSong } from "@/lib/songs";
import { LineView, type LineState } from "./line-view";

/**
 * The song's lines, lit up in time with the audio (FE-3.4). The full controls (morphing
 * play button, wave timeline, speed, loop, translation toggle) arrive with the player bar
 * in FE-3.5; until then a plain Play button starts the song.
 */
export function LyricsPlayer({ song }: { song: PublicSong }) {
  const [audio, setAudio] = useState<HTMLAudioElement | null>(null);
  const playing = usePlaying(audio);
  const { activeIndex, activeLineRef } = useLineSync(audio, song.lines);
  const { following, backToSong } = useAutoScroll(activeIndex, activeLineRef);
  const [lastSung, setLastSung] = useState(-1);
  if (activeIndex !== -1 && activeIndex !== lastSung) setLastSung(activeIndex);

  const stateOf = (index: number): LineState =>
    index === activeIndex ? "active" : index <= lastSung ? "done" : "upcoming";

  return (
    <div className="flex flex-col items-center gap-8">
      <audio ref={setAudio} src={song.audioUrl} preload="metadata" />

      <button
        type="button"
        onClick={() => (audio?.paused ? void audio.play() : audio?.pause())}
        className="inline-flex h-11 items-center gap-2 rounded-pill bg-violet px-6 text-label text-ink transition-colors duration-(--duration-instant) hover:bg-violet/90"
      >
        {playing ? <Pause size={18} aria-hidden /> : <Play size={18} aria-hidden />}
        {playing ? "Pause" : "Play"}
      </button>

      <ol lang="de" aria-label="Lyrics" className="flex w-full max-w-(--width-lyrics) flex-col">
        {song.lines.map((line, i) => (
          <LineView
            key={line.index}
            line={line}
            state={stateOf(i)}
            showTranslation
            ref={i === activeIndex ? (el) => void (activeLineRef.current = el) : undefined}
          />
        ))}
      </ol>

      <AnimatePresence>
        {!following && playing && activeIndex !== -1 && (
          <motion.button
            type="button"
            onClick={backToSong}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0, transition: { duration: duration.standard, ease: ease.out } }}
            exit={{ opacity: 0, transition: { duration: duration.exit, ease: ease.in } }}
            className="fixed bottom-[calc(var(--height-tab-bar)+16px)] left-1/2 z-20 -translate-x-1/2 rounded-pill bg-plum px-6 py-3 text-label text-ink shadow-overlay md:bottom-6"
          >
            Back to the song
          </motion.button>
        )}
      </AnimatePresence>
    </div>
  );
}
