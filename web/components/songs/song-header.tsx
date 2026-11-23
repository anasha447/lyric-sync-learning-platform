"use client";

import { motion, useReducedMotion, useScroll, useTransform } from "motion/react";
import { useRef } from "react";
import { LevelBadge } from "@/components/level-badge";
import { artworkFor } from "@/lib/artwork";
import { duration } from "@/lib/motion";
import type { PublicSong } from "@/lib/songs";
import { SongCover } from "./song-cover";

/**
 * The song page header (DESIGN.md → Layout → Song page). Desktop: a 280 px cover beside the
 * title, artist and level. Phones: a nearly full-width cover that, as the lyrics scroll, shrinks
 * away while a compact header with a 40 px thumbnail takes its place: the one scroll-driven
 * animation the No-Hijack rule allows. Behind it all, the ambient glow.
 */
export function SongHeader({ song }: { song: PublicSong }) {
  const reduceMotion = useReducedMotion() ?? false;
  const coverRef = useRef<HTMLDivElement>(null);
  const { scrollYProgress } = useScroll({ target: coverRef, offset: ["start start", "end start"] });
  const coverScale = useTransform(scrollYProgress, [0, 1], [1, reduceMotion ? 1 : 0.6]);
  const coverOpacity = useTransform(scrollYProgress, [0.2, 0.9], [1, 0]);
  const compactOpacity = useTransform(scrollYProgress, [0.7, 1], [0, 1]);

  return (
    <header className="relative isolate">
      <AmbientGlow slug={song.slug} />

      <div className="flex flex-col items-center gap-6 md:flex-row md:items-end md:gap-8">
        <motion.div
          ref={coverRef}
          style={{ scale: coverScale, opacity: coverOpacity, transformOrigin: "top center" }}
          className="w-full max-w-[calc(100%-32px)] md:w-[280px] md:max-w-none md:shrink-0 md:opacity-100!"
        >
          <SongCover slug={song.slug} title={song.title} className="rounded-lg" />
        </motion.div>
        <div className="flex flex-col items-center gap-2 text-center md:items-start md:text-left">
          <h1 lang="de" className="font-display text-display text-ink">
            {song.title}
          </h1>
          <p className="font-display text-title text-ink-muted">{song.artist}</p>
          <LevelBadge level={song.level} />
        </div>
      </div>

      {/* phones: the compact header the cover shrinks into */}
      <motion.div
        aria-hidden
        style={{ opacity: compactOpacity }}
        className="pointer-events-none fixed inset-x-0 top-0 z-20 flex h-14 items-center gap-3 border-b border-hairline bg-dusk px-4 md:hidden"
      >
        <SongCover slug={song.slug} title={song.title} decorative className="size-10 shrink-0 rounded-sm" />
        <p lang="de" className="truncate font-display text-title text-ink">
          {song.title}
        </p>
      </motion.div>
    </header>
  );
}

/**
 * A soft radial light in the cover's main colour behind the header: top-centre, 70 % of the
 * viewport wide, 35 % strong, heavily blurred, fading into night. It fades in over
 * duration-ambient, so moving between songs crossfades the light.
 */
function AmbientGlow({ slug }: { slug: string }) {
  const { from } = artworkFor(slug);
  return (
    <motion.div
      key={slug}
      aria-hidden
      initial={{ opacity: 0 }}
      animate={{ opacity: 0.35, transition: { duration: duration.ambient } }}
      className="pointer-events-none absolute -top-24 left-1/2 -z-10 aspect-[2/1] w-[70vw] -translate-x-1/2 rounded-pill blur-3xl"
      style={{ background: `radial-gradient(closest-side, var(--color-${from}), transparent)` }}
    />
  );
}
