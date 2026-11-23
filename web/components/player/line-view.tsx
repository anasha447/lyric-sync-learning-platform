import type { Ref } from "react";
import { cn } from "@/lib/utils";
import type { Line } from "@/lib/songs";

export type LineState = "upcoming" | "active" | "done";

/**
 * One line of the song (GLOSSARY: Line). The active line grows from Lyric to Lyric Active
 * and turns ink; a karaoke underline fills as it's sung (the singing line, DESIGN.md → Motion).
 * Words become buttons with the word card in FE-4.8.
 */
export function LineView({
  line,
  state,
  showTranslation,
  ref,
}: {
  line: Line;
  state: LineState;
  showTranslation: boolean;
  ref?: Ref<HTMLLIElement>;
}) {
  const active = state === "active";
  return (
    <li
      ref={ref}
      aria-current={active ? "true" : undefined}
      data-state={state}
      className="flex flex-col items-center gap-1 py-3 text-center"
    >
      <p className="text-balance">
        <span
          data-underline={active ? "" : undefined}
          className={cn(
            "pb-1 font-display transition-[font-size,color] duration-(--duration-standard) ease-out",
            active ? "text-lyric-active text-ink" : "text-lyric text-ink-muted",
          )}
          // The karaoke underline is a background on the inline text: when the line wraps, the
          // rows form one strip, so it fills the first row and carries on into the next. Linear,
          // because it is time; useLineSync sets --line-progress.
          style={
            active
              ? {
                  backgroundImage: "linear-gradient(var(--color-karaoke), var(--color-karaoke))",
                  backgroundRepeat: "no-repeat",
                  backgroundPosition: "left bottom",
                  backgroundSize: "calc(var(--line-progress, 0) * 100%) 3px",
                }
              : undefined
          }
        >
          {line.instrumental ? <span aria-label="Instrumental">♪</span> : line.text}
        </span>
      </p>
      {showTranslation && line.translation && !line.instrumental && (
        <span lang="en" className="max-w-[65ch] text-translation text-teal-light">
          {line.translation}
        </span>
      )}
    </li>
  );
}
