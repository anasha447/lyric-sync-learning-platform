import type { Metadata } from "next";
import { EmptyState } from "@/components/empty-state";

export const metadata: Metadata = { title: "Vocabulary" };

export default function VocabularyPage() {
  return (
    <section className="flex flex-col gap-6">
      <h1 className="font-display text-headline text-ink">Vocabulary</h1>
      <EmptyState line="Tap a word in any song to see what it means." action={{ label: "Find a song", href: "/library" }} />
    </section>
  );
}
