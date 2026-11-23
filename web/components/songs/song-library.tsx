"use client";

import { useInfiniteQuery } from "@tanstack/react-query";
import { motion, useReducedMotion } from "motion/react";
import { useState, type KeyboardEvent } from "react";
import { duration, ease, staggerDelay } from "@/lib/motion";
import { LEVELS, songListQuery, type Level } from "@/lib/songs";
import { cn } from "@/lib/utils";
import { SongCard, SongCardSkeleton } from "./song-card";

type Tab = Level | "all";
const TABS: Tab[] = ["all", ...LEVELS];
const tabLabel = (tab: Tab) => (tab === "all" ? "All levels" : tab);

/** 2 columns on phones, 3 on tablets, 4 on desktop, 5 above 1440 px (DESIGN.md → Layout). */
const grid = "grid grid-cols-2 gap-4 md:grid-cols-3 lg:grid-cols-4 lg:gap-6 xl:grid-cols-5";

/**
 * The library: level tabs and the song grid. The guest's own level becomes the default tab
 * with onboarding (FE-6.1); until then it opens on all levels.
 */
export function SongLibrary() {
  const [tab, setTab] = useState<Tab>("all");
  const query = useInfiniteQuery(songListQuery(tab === "all" ? undefined : tab));
  const reduceMotion = useReducedMotion() ?? false;
  const songs = query.data?.pages.flatMap((p) => p.items) ?? [];

  // arrow keys move between tabs, as screen-reader users expect from a tab list
  const onTabKey = (event: KeyboardEvent<HTMLButtonElement>, index: number) => {
    const step = event.key === "ArrowRight" ? 1 : event.key === "ArrowLeft" ? -1 : 0;
    if (!step) return;
    event.preventDefault();
    const next = (index + step + TABS.length) % TABS.length;
    setTab(TABS[next]);
    document.getElementById(`level-tab-${TABS[next]}`)?.focus();
  };

  return (
    <div className="flex flex-col gap-6">
      <div role="tablist" aria-label="Level" className="flex flex-wrap gap-2">
        {TABS.map((t, i) => (
          <button
            key={t}
            id={`level-tab-${t}`}
            type="button"
            role="tab"
            aria-selected={tab === t}
            aria-controls="song-grid"
            tabIndex={tab === t ? 0 : -1}
            onClick={() => setTab(t)}
            onKeyDown={(event) => onTabKey(event, i)}
            className={cn(
              "inline-flex h-11 items-center rounded-pill px-4 text-label transition-colors duration-(--duration-instant) md:px-5",
              tab === t
                ? "bg-violet text-ink"
                : "border border-hairline text-violet-light hover:bg-plum",
            )}
          >
            {tabLabel(t)}
          </button>
        ))}
      </div>

      <section id="song-grid" role="tabpanel" aria-labelledby={`level-tab-${tab}`} aria-busy={query.isPending}>
        {query.isPending ? (
          <div className={grid}>
            {Array.from({ length: 8 }, (_, i) => (
              <SongCardSkeleton key={i} />
            ))}
          </div>
        ) : query.isError ? (
          <div role="alert" className="flex flex-col items-start gap-4 rounded-lg border border-hairline bg-dusk p-6">
            <p className="text-body text-ink-muted">We couldn&apos;t load the songs.</p>
            <button
              type="button"
              onClick={() => void query.refetch()}
              className="inline-flex h-11 items-center rounded-pill bg-violet px-6 text-label text-ink"
            >
              Try again
            </button>
          </div>
        ) : songs.length === 0 ? (
          <div className="flex flex-col items-start gap-4 rounded-lg border border-hairline bg-dusk p-6">
            <p className="text-body text-ink-muted">
              {tab === "all" ? "Songs appear here as soon as they're published." : `No ${tab} songs yet.`}
            </p>
            {tab !== "all" && (
              <button
                type="button"
                onClick={() => setTab("all")}
                className="inline-flex h-11 items-center rounded-pill bg-violet px-6 text-label text-ink"
              >
                Show all levels
              </button>
            )}
          </div>
        ) : (
          <>
            <ul className={grid}>
              {songs.map((song, i) => (
                <motion.li
                  key={song.slug}
                  initial={reduceMotion ? { opacity: 0 } : { opacity: 0, y: 8 }}
                  animate={{
                    opacity: 1,
                    y: 0,
                    transition: { duration: duration.slow, ease: ease.out, delay: staggerDelay(i % 20) },
                  }}
                >
                  <SongCard song={song} />
                </motion.li>
              ))}
            </ul>
            {query.hasNextPage && (
              <div className="mt-8 flex justify-center">
                <button
                  type="button"
                  onClick={() => void query.fetchNextPage()}
                  disabled={query.isFetchingNextPage}
                  className="inline-flex h-11 items-center rounded-pill border border-hairline px-6 text-label text-violet-light hover:bg-plum disabled:opacity-40"
                >
                  Show more songs
                </button>
              </div>
            )}
          </>
        )}
      </section>
    </div>
  );
}
