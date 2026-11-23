/** Generated artwork for songs without a cover (DESIGN.md → Shapes → Generated artwork). */
export const ARTWORK_COLOURS = ["violet", "karaoke", "plum", "teal", "amber"] as const;
export const ARTWORK_SHAPES = ["circle", "arc", "wave", "bloom", "stripe", "orbit"] as const;

export type ArtworkColour = (typeof ARTWORK_COLOURS)[number];
export type ArtworkShape = (typeof ARTWORK_SHAPES)[number];
export type Artwork = { from: ArtworkColour; to: ArtworkColour; angle: number; shape: ArtworkShape };

/** FNV-1a: a small, stable string hash, so the same slug always draws the same art. */
function hash(text: string): number {
  let h = 0x811c9dc5;
  for (let i = 0; i < text.length; i++) {
    h ^= text.charCodeAt(i);
    h = Math.imul(h, 0x01000193);
  }
  return h >>> 0;
}

export function artworkFor(slug: string): Artwork {
  const h = hash(slug);
  const first = h % ARTWORK_COLOURS.length;
  // the second colour is one of the other four, so the two never match
  const second = (first + 1 + (Math.floor(h / 5) % (ARTWORK_COLOURS.length - 1))) % ARTWORK_COLOURS.length;
  return {
    from: ARTWORK_COLOURS[first],
    to: ARTWORK_COLOURS[second],
    angle: (Math.floor(h / 20) % 24) * 15,
    shape: ARTWORK_SHAPES[Math.floor(h / 480) % ARTWORK_SHAPES.length],
  };
}
