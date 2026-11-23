/*
 * A line has two jobs, time and meaning (DESIGN.md → Components → Lyric line). On a phone a
 * finger can't aim between words, so a tap anywhere plays from the line and words open their
 * card by press and hold (FE-4.8). A mouse can aim, so a click on a word is kept for its card,
 * and the empty space and the translation play from the line.
 */

const isTouch = (pointerType: string | undefined) => pointerType === "touch" || pointerType === "pen";

/** Whether a click on a line plays from it. `onWordText`: the click landed on the lyric's words. */
export function lineClickPlays(pointerType: string | undefined, onWordText: boolean): boolean {
  return isTouch(pointerType) || !onWordText;
}

/** A tap on the words with translations off shows that line's translation until the next line. */
export function lineClickRevealsTranslation(pointerType: string | undefined, onWordText: boolean): boolean {
  return isTouch(pointerType) && onWordText;
}
