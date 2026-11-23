import { clsx, type ClassValue } from "clsx";
import { extendTailwindMerge } from "tailwind-merge";

// Teach tailwind-merge our custom text sizes, so `text-label` and `text-ink` don't cancel each other.
const twMerge = extendTailwindMerge({
  extend: {
    classGroups: {
      "font-size": [{ text: ["display", "headline", "title", "lyric-active", "lyric", "body", "translation", "label", "mono"] }],
    },
  },
});

/** Joins class names and resolves Tailwind conflicts (shadcn/ui convention). */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
