"use client";

import { useQuery } from "@tanstack/react-query";
import NotFound from "@/app/not-found";
import { LevelBadge } from "@/components/level-badge";
import { isNotFound } from "@/lib/api";
import { songQuery } from "@/lib/songs";
import { LyricsPlayer } from "./lyrics-player";

/** The song page. Data is fetched in the browser, so the Worker's render stays light (ADR-0012). */
export function SongScreen({ slug }: { slug: string }) {
  const { data: song, error, isPending, refetch, isRefetching } = useQuery(songQuery(slug));

  if (isPending) return <SongSkeleton />;
  if (error && isNotFound(error)) return <NotFound />;
  if (error) {
    return (
      <div role="alert" className="mx-auto flex max-w-(--width-lyrics) flex-col items-center gap-4 py-16 text-center">
        <p className="text-body text-ink-muted">We couldn&apos;t load this song.</p>
        <button
          type="button"
          onClick={() => void refetch()}
          disabled={isRefetching}
          className="inline-flex h-11 items-center rounded-pill bg-violet px-6 text-label text-ink disabled:opacity-60"
        >
          Try again
        </button>
      </div>
    );
  }

  return (
    <article className="flex flex-col gap-8">
      {/* The full header (cover, ambient glow) comes with FE-3.6. */}
      <header className="flex flex-col items-center gap-2 text-center">
        <h1 lang="de" className="font-display text-display text-ink">
          {song.title}
        </h1>
        <p className="font-display text-title text-ink-muted">{song.artist}</p>
        <LevelBadge level={song.level} />
      </header>
      <LyricsPlayer song={song} />
    </article>
  );
}

function SongSkeleton() {
  return (
    <div aria-busy="true" aria-label="Loading the song" className="mx-auto flex w-full max-w-(--width-lyrics) flex-col items-center gap-4">
      <div className="skeleton h-12 w-2/3 rounded-md" />
      <div className="skeleton h-6 w-1/3 rounded-md" />
      <div className="mt-8 flex w-full flex-col items-center gap-6">
        {[80, 65, 90, 70, 55].map((w) => (
          <div key={w} className="skeleton h-6 rounded-md" style={{ width: `${w}%` }} />
        ))}
      </div>
    </div>
  );
}
