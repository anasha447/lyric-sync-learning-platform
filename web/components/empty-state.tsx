import Link from "next/link";

/** One line and one action (DESIGN.md → Components → Empty and error states). */
export function EmptyState({ line, action }: { line: string; action?: { label: string; href: string } }) {
  return (
    <div className="flex flex-col items-start gap-4 rounded-lg border border-hairline bg-dusk p-6">
      <p className="text-body text-ink-muted">{line}</p>
      {action && (
        <Link
          href={action.href}
          className="inline-flex h-11 items-center rounded-pill bg-violet px-6 text-label text-ink transition-colors duration-(--duration-instant) hover:bg-violet/90"
        >
          {action.label}
        </Link>
      )}
    </div>
  );
}
