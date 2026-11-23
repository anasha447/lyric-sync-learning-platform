"use client";

import { ChevronDown } from "lucide-react";
import { AnimatePresence, motion } from "motion/react";
import { useEffect, useRef, useState } from "react";
import { duration, ease } from "@/lib/motion";
import type { PlayerControls } from "@/lib/player/use-player-controls";
import { useMediaQuery } from "@/lib/player/use-player-controls";
import type { PublicSong } from "@/lib/songs";
import { PlayButton } from "./play-button";
import { PlayerToggles } from "./player-toggles";
import { WaveTimeline } from "./wave-timeline";

type Props = {
  song: PublicSong;
  audio: HTMLAudioElement | null;
  controls: PlayerControls;
  currentLine: number;
};

/**
 * Tablet and desktop: the 88 px player bar along the bottom. Phones: the mini player above
 * the tab bar, which expands into a sheet (DESIGN.md → Layout → Song page). Only one is
 * mounted, so there's one timeline drawing and one slider for screen readers.
 */
export function Player(props: Props) {
  const wide = useMediaQuery("(min-width: 768px)");
  return wide ? <PlayerBar {...props} /> : <MiniPlayer {...props} />;
}

const blurredDusk = "border-hairline bg-dusk/82 backdrop-blur-[20px]";
const hasTranslations = (song: PublicSong) => song.lines.some((l) => l.translation && !l.instrumental);

function SongTitle({ song }: { song: PublicSong }) {
  return (
    <div className="min-w-0">
      <p lang="de" className="truncate font-display text-title text-ink">
        {song.title}
      </p>
      <p className="truncate text-label text-ink-muted">{song.artist}</p>
    </div>
  );
}

function PlayerBar({ song, audio, controls, currentLine }: Props) {
  return (
    <section
      aria-label="Player"
      className={`fixed right-0 bottom-0 left-(--width-sidebar-collapsed) z-30 grid h-(--height-player-bar) grid-cols-[minmax(0,1fr)_minmax(0,2fr)_auto] items-center gap-6 border-t px-6 lg:left-(--width-sidebar) ${blurredDusk}`}
    >
      <SongTitle song={song} />
      <div className="flex min-w-0 items-center gap-4">
        <PlayButton playing={controls.playing} onToggle={controls.toggle} />
        <WaveTimeline audio={audio} duration={controls.duration} playing={controls.playing} onSeek={controls.seekTo} />
      </div>
      <PlayerToggles controls={controls} currentLine={currentLine} hasTranslations={hasTranslations(song)} />
    </section>
  );
}

function MiniPlayer({ song, audio, controls, currentLine }: Props) {
  const [open, setOpen] = useState(false);
  const openerRef = useRef<HTMLButtonElement>(null);
  const closeRef = useRef<HTMLButtonElement>(null);

  // focus moves into the sheet when it opens and back to the mini player when it closes
  useEffect(() => {
    if (open) closeRef.current?.focus();
  }, [open]);

  const close = () => {
    setOpen(false);
    openerRef.current?.focus();
  };

  return (
    <>
      {/*
        76 px pill: the title on top (opens the sheet), the 56 px play button on the right, and the
        wave timeline along the bottom edge (DESIGN.md → Components → Mini player). The strip's
        44 px touch area reaches into the 8 px gap above the tab bar, never into the tab bar, and
        sits above the title's lower edge; the play button sits above everything.
      */}
      <section
        aria-label="Player"
        className={`fixed inset-x-2 bottom-[calc(var(--height-tab-bar)+8px)] z-30 h-(--height-mini-player) rounded-pill border ${blurredDusk}`}
      >
        <button
          ref={openerRef}
          type="button"
          aria-expanded={open}
          aria-haspopup="dialog"
          onClick={() => setOpen(true)}
          className="absolute top-0 right-[72px] left-0 z-10 flex h-12 items-center rounded-l-pill pt-2 pl-6 text-left"
        >
          <span className="sr-only">Open the player: </span>
          <SongTitle song={song} />
        </button>
        <div className="absolute right-[76px] -bottom-2 left-6 z-20">
          <WaveTimeline
            audio={audio}
            duration={controls.duration}
            playing={controls.playing}
            onSeek={controls.seekTo}
            size="mini"
          />
        </div>
        <div className="absolute top-1/2 right-2 z-30 -translate-y-1/2">
          <PlayButton playing={controls.playing} onToggle={controls.toggle} size="mini" />
        </div>
      </section>

      <AnimatePresence>
        {open && (
          <>
            <motion.div
              aria-hidden
              onClick={close}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1, transition: { duration: duration.slow, ease: ease.out } }}
              exit={{ opacity: 0, transition: { duration: duration.standard, ease: ease.in } }}
              className="fixed inset-0 z-40 bg-night/60"
            />
            <motion.div
              role="dialog"
              aria-modal="true"
              aria-label="Player"
              onKeyDown={(event) => event.key === "Escape" && close()}
              initial={{ y: "100%" }}
              animate={{ y: 0, transition: { duration: duration.slow, ease: ease.out } }}
              exit={{ y: "100%", transition: { duration: duration.standard, ease: ease.in } }}
              className="fixed inset-x-0 bottom-0 z-50 flex flex-col gap-6 rounded-t-xl border-t border-hairline bg-dusk px-4 pt-3 pb-[calc(env(safe-area-inset-bottom)+24px)] shadow-overlay"
            >
              <button
                ref={closeRef}
                type="button"
                onClick={close}
                aria-label="Close the player"
                className="mx-auto inline-flex size-11 items-center justify-center rounded-pill text-ink-muted hover:text-ink"
              >
                <ChevronDown size={24} strokeWidth={1.5} aria-hidden />
              </button>
              <div className="text-center">
                <SongTitle song={song} />
              </div>
              <WaveTimeline audio={audio} duration={controls.duration} playing={controls.playing} onSeek={controls.seekTo} size="sheet" />
              <div className="flex justify-center">
                <PlayButton playing={controls.playing} onToggle={controls.toggle} />
              </div>
              <div className="flex justify-center">
                <PlayerToggles controls={controls} currentLine={currentLine} hasTranslations={hasTranslations(song)} />
              </div>
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </>
  );
}
