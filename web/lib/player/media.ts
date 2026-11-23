/*
 * Writes to the <audio> element. Hooks receive the element as an argument, and the React
 * Compiler's lint forbids changing hook arguments; for a DOM media element, changing it is
 * exactly the job, so the writes live in these plain functions.
 */

export function seekAudio(audio: HTMLAudioElement, seconds: number): void {
  audio.currentTime = seconds;
}

/** Slower without the "chipmunk" or deep-voice effect: the pitch stays the same. */
export function setAudioSpeed(audio: HTMLAudioElement, rate: number): void {
  audio.preservesPitch = true;
  audio.playbackRate = rate;
}
