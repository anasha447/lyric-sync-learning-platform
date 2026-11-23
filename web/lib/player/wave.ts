/** The wave timeline's numbers (DESIGN.md → Motion → The wave timeline). */
export const WAVE = { wavelength: 24, amplitude: 3, secondsPerWavelength: 1.2 } as const;

const STEP = 2;
const round = (n: number) => Math.round(n * 100) / 100;

/**
 * The played part of the timeline as an SVG path: a sine wave from x = 0 to `width`,
 * centred on `midY`. `phase` (px) shifts it right so the wave flows; amplitude 0 is a
 * straight line, which is what the wave flattens into on pause.
 */
export function wavePath(width: number, midY: number, amplitude: number, phase: number): string {
  if (width <= 0) return "";
  const y = (x: number) => round(midY - amplitude * Math.sin((2 * Math.PI * (x - phase)) / WAVE.wavelength));
  const parts: string[] = [];
  for (let x = 0; x < width; x += STEP) parts.push(`${parts.length === 0 ? "M" : "L"}${round(x)} ${y(x)}`);
  parts.push(`L${round(width)} ${y(width)}`);
  return parts.join(" ");
}
