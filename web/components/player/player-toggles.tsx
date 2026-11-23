"use client";

import { Languages, Repeat1 } from "lucide-react";
import type { ReactNode } from "react";
import { cn } from "@/lib/utils";
import type { PlayerControls } from "@/lib/player/use-player-controls";

/**
 * Loop line, speed and translations. A pressed toggle gets a filled plum pill as well as
 * the violet-light colour, so its state never depends on colour alone.
 */
export function PlayerToggles({
  controls,
  currentLine,
  hasTranslations,
}: {
  controls: PlayerControls;
  /** The line being sung, or the last one sung; -1 before the first. */
  currentLine: number;
  hasTranslations: boolean;
}) {
  const looping = controls.loopIndex !== null;
  return (
    <div className="flex items-center gap-1">
      <ToggleButton
        label="Loop this line"
        pressed={looping}
        disabled={!looping && currentLine === -1}
        onClick={() => controls.toggleLoop(currentLine)}
      >
        <Repeat1 size={20} strokeWidth={1.5} aria-hidden />
      </ToggleButton>
      <button
        type="button"
        onClick={controls.cycleSpeed}
        aria-label={`Speed ${controls.speed} times. Change speed`}
        className={cn(
          "inline-flex h-11 min-w-14 items-center justify-center rounded-pill px-3 font-mono text-mono transition-colors duration-(--duration-instant)",
          controls.speed === 1 ? "text-ink-muted hover:bg-plum/50 hover:text-ink" : "bg-plum text-violet-light",
        )}
      >
        {controls.speed}×
      </button>
      {hasTranslations && (
        <ToggleButton label="Show translations" pressed={controls.showTranslations} onClick={controls.toggleTranslations}>
          <Languages size={20} strokeWidth={1.5} aria-hidden />
        </ToggleButton>
      )}
    </div>
  );
}

function ToggleButton({
  label,
  pressed,
  disabled,
  onClick,
  children,
}: {
  label: string;
  pressed: boolean;
  disabled?: boolean;
  onClick: () => void;
  children: ReactNode;
}) {
  return (
    <button
      type="button"
      aria-label={label}
      aria-pressed={pressed}
      title={label}
      disabled={disabled}
      onClick={onClick}
      className={cn(
        "inline-flex size-11 items-center justify-center rounded-pill transition-colors duration-(--duration-instant) disabled:opacity-40",
        pressed ? "bg-plum text-violet-light" : "text-ink-muted enabled:hover:bg-plum/50 enabled:hover:text-ink",
      )}
    >
      {children}
    </button>
  );
}
