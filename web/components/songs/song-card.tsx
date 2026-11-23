import Link from "next/link";
import { LevelBadge } from "@/components/level-badge";
import type { SongSummary } from "@/lib/songs";
import { SongCover } from "./song-cover";

/**
 * Cover, title and level; deliberately no artist (DESIGN.md → Components → Song card).
 * The whole card is one link. Hover lifts the cover 4 px, zooms it 1.03 inside its frame,
 * and strengthens the border.
 */
export function SongCard({ song }: { song: SongSummary }) {
  return (
    <Link
      href={`/songs/${song.slug}`}
      className="group flex flex-col gap-3 rounded-lg border border-hairline bg-plum p-3 transition-colors duration-(--duration-instant) hover:border-hairline-strong"
    >
      <div className="transition-transform duration-(--duration-instant) ease-out group-hover:-translate-y-1">
        <div className="overflow-hidden rounded-md">
          <SongCover
            slug={song.slug}
            title={song.title}
            decorative
            className="transition-transform duration-(--duration-instant) ease-out group-hover:scale-[1.03]"
          />
        </div>
      </div>
      <p lang="de" className="line-clamp-2 font-display text-title text-ink">
        {song.title}
      </p>
      {song.level && (
        <span>
          <LevelBadge level={song.level} />
        </span>
      )}
    </Link>
  );
}

export function SongCardSkeleton() {
  return (
    <div aria-hidden className="flex flex-col gap-3 rounded-lg border border-hairline bg-plum p-3">
      <div className="skeleton aspect-square rounded-md" />
      <div className="skeleton h-5 w-3/4 rounded-sm" />
      <div className="skeleton h-5 w-10 rounded-pill" />
    </div>
  );
}
