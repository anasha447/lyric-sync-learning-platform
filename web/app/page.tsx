import { EmptyState } from "@/components/empty-state";

/** Home grows section by section: continue listening and songs for your level arrive in FE-3.6 and FE-6.1. */
export default function HomePage() {
  return (
    <section className="flex flex-col gap-6">
      <h1 className="font-display text-headline text-ink">Welcome back</h1>
      <EmptyState line="Pick a song and the lyrics will light up as it plays." action={{ label: "Open the library", href: "/library" }} />
    </section>
  );
}
