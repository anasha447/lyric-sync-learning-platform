import { artworkFor, type Artwork } from "@/lib/artwork";
import { cn } from "@/lib/utils";

/**
 * A song's square cover. Uploaded covers arrive with the admin upload (Sprint 6); until then
 * every song shows its generated artwork, drawn as SVG on night, without text.
 * `decorative` when the title is already visible in the same link: alt text would repeat it.
 */
export function SongCover({
  slug,
  title,
  decorative = false,
  className,
}: {
  slug: string;
  title: string;
  decorative?: boolean;
  className?: string;
}) {
  const art = artworkFor(slug);
  return (
    <div
      className={cn("aspect-square overflow-hidden bg-night", className)}
      {...(decorative ? { "aria-hidden": true } : { role: "img", "aria-label": `Cover of ${title}` })}
    >
      <GeneratedArt slug={slug} art={art} />
    </div>
  );
}

const colour = (name: string) => `var(--color-${name})`;

function GeneratedArt({ slug, art }: { slug: string; art: Artwork }) {
  // ids must be unique on a page that shows many covers
  const id = `art-${slug}`;
  const fill = `url(#${id})`;
  return (
    <svg viewBox="0 0 100 100" className="size-full" preserveAspectRatio="xMidYMid slice">
      <defs>
        <linearGradient id={id} gradientTransform={`rotate(${art.angle} 0.5 0.5)`}>
          <stop offset="0" stopColor={colour(art.from)} />
          <stop offset="1" stopColor={colour(art.to)} />
        </linearGradient>
      </defs>
      <rect width="100" height="100" fill={colour("night")} />
      <Shape shape={art.shape} fill={fill} />
    </svg>
  );
}

function Shape({ shape, fill }: { shape: Artwork["shape"]; fill: string }) {
  switch (shape) {
    case "circle":
      return <circle cx="50" cy="50" r="32" fill={fill} opacity="0.9" />;
    case "arc":
      return <path d="M14 78 A40 40 0 0 1 86 78" fill="none" stroke={fill} strokeWidth="14" strokeLinecap="round" />;
    case "wave":
      return (
        <path
          d="M-4 56 C 12 32, 28 32, 44 56 S 76 80, 104 50"
          fill="none"
          stroke={fill}
          strokeWidth="12"
          strokeLinecap="round"
        />
      );
    case "bloom":
      return (
        <g opacity="0.85">
          {[0, 60, 120].map((r) => (
            <ellipse key={r} cx="50" cy="50" rx="34" ry="13" fill={fill} opacity="0.7" transform={`rotate(${r} 50 50)`} />
          ))}
        </g>
      );
    case "stripe":
      return (
        <g transform="rotate(-30 50 50)" opacity="0.9">
          {[24, 46, 68].map((y, i) => (
            <rect key={y} x="-10" y={y} width="120" height="12" rx="6" fill={fill} opacity={1 - i * 0.25} />
          ))}
        </g>
      );
    case "orbit":
      return (
        <g>
          <circle cx="50" cy="50" r="30" fill="none" stroke={fill} strokeWidth="5" opacity="0.8" />
          <circle cx="50" cy="50" r="13" fill={fill} />
          <circle cx="76" cy="35" r="6" fill={fill} />
        </g>
      );
  }
}
