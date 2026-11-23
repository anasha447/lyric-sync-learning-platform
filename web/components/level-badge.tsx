import type { Level } from "@/lib/songs";

/** Amber pill with the CEFR level (DESIGN.md → Components → Level badge). */
export function LevelBadge({ level }: { level: Level }) {
  return (
    <span className="inline-flex items-center rounded-pill bg-amber px-[10px] py-[2px] text-label text-night">
      <span className="sr-only">Level </span>
      {level}
    </span>
  );
}
