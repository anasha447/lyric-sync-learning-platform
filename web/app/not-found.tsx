import { EmptyState } from "@/components/empty-state";

export default function NotFound() {
  return (
    <section className="flex flex-col gap-6">
      <h1 className="font-display text-headline text-ink">This page isn&apos;t playing.</h1>
      <EmptyState line="The link may be old, or the song isn't published." action={{ label: "Go home", href: "/" }} />
    </section>
  );
}
