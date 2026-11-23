/** Where the active line rests: about 40 % down the viewport (DESIGN.md → Layout → Song page). */
export const ACTIVE_LINE_POSITION = 0.4;

/**
 * The page scroll position that puts the middle of a line at 40 % of the viewport.
 * `lineTop` is the line's distance from the top of the document.
 */
export function scrollTargetFor(lineTop: number, lineHeight: number, viewportHeight: number): number {
  return Math.max(0, Math.round(lineTop + lineHeight / 2 - viewportHeight * ACTIVE_LINE_POSITION));
}

/**
 * Keys that scroll the page: pressing one means the learner took over scrolling.
 * Space isn't one: on the song page it plays and pauses (DESIGN.md → Play button).
 */
export const SCROLL_KEYS = new Set(["ArrowUp", "ArrowDown", "PageUp", "PageDown", "Home", "End"]);
