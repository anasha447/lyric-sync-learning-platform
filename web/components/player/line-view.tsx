import { Play, Repeat1 } from "lucide-react";
import type { MouseEvent, Ref } from "react";
import { lineClickPlays, lineClickRevealsTranslation } from "@/lib/player/line-click";
import { cn } from "@/lib/utils";
import type { Line } from "@/lib/songs";

export type LineState = "upcoming" | "active" | "done";

/**
 * One line of the song (GLOSSARY: Line). The active line grows from Lyric to Lyric Active
 * and turns ink; a karaoke underline fills as it's sung (the singing line, DESIGN.md → Motion).
 *
 * Playing from the line (DESIGN.md → Components → Lyric line): a tap anywhere on a phone; on a
 * PC ▶, the translation or the empty space, since a click on a word is kept for its word card
 * (FE-4.8); for keyboards and screen readers the "Play from: …" button.
 */
export function LineView({
  line,
  state,
  showTranslation,
  looped = false,
  onPlay,
  ref,
}: {
  line: Line;
  state: LineState;
  showTranslation: boolean;
  /** The line the learner is looping: marked with an icon, not colour alone. */
  looped?: boolean;
  /** Plays from this line; `revealTranslation` when a tap on its words should show the translation. */
  onPlay?: (revealTranslation: boolean) => void;
  ref?: Ref<HTMLLIElement>;
}) {
  const active = state === "active";
  const label = line.instrumental ? "Instrumental" : line.text;

  const onClick = (event: MouseEvent<HTMLLIElement>) => {
    if (!onPlay) return;
    // a click event from a pointer carries its type ("touch", "pen", "mouse"); a keyboard one doesn't
    const pointerType = (event.nativeEvent as PointerEvent).pointerType;
    const onWords = (event.target as HTMLElement).closest("[data-words]") !== null;
    if (lineClickPlays(pointerType, onWords)) onPlay(lineClickRevealsTranslation(pointerType, onWords));
  };

  return (
    <li
      ref={ref}
      aria-current={active ? "true" : undefined}
      data-state={state}
      onClick={onClick}
      className={cn(
        "group relative flex flex-col items-center gap-1 rounded-md py-3 text-center select-none [-webkit-touch-callout:none]",
        onPlay && "cursor-pointer transition-colors duration-(--duration-instant) active:bg-plum/30 md:hover:bg-plum/30",
      )}
    >
      {onPlay && (
        <button
          type="button"
          aria-label={`Play from: ${label}`}
          onClick={(event) => {
            event.stopPropagation();
            onPlay(false);
          }}
          className={cn(
            "absolute top-3 left-0 inline-flex size-11 items-center justify-center rounded-pill text-ink-muted transition-opacity duration-(--duration-instant) hover:text-ink max-md:sr-only",
            active ? "opacity-100" : "opacity-0 group-hover:opacity-100 focus-visible:opacity-100",
          )}
        >
          <Play size={18} strokeWidth={1.5} aria-hidden />
        </button>
      )}
      <p className="text-balance md:px-12">
        <span
          data-words
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
        {looped && (
          <span className="ml-2 inline-flex translate-y-0.5 text-violet-light" title="Looping this line">
            <Repeat1 size={18} strokeWidth={1.5} aria-hidden />
            <span className="sr-only"> (looping)</span>
          </span>
        )}
      </p>
      {showTranslation && line.translation && !line.instrumental && (
        <span lang="en" className="max-w-[65ch] text-translation text-teal-light md:px-12">
          {line.translation}
        </span>
      )}
    </li>
  );
}
