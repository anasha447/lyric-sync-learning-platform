import type { Metadata } from "next";
import { EmptyState } from "@/components/empty-state";

export const metadata: Metadata = { title: "Library" };

export default function LibraryPage() {
  return (
    <section className="flex flex-col gap-6">
      <h1 className="font-display text-headline text-ink">Library</h1>
      <EmptyState line="Songs appear here as soon as they're published." />
    </section>
  );
}
