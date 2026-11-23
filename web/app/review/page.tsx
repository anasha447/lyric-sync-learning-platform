import type { Metadata } from "next";
import { EmptyState } from "@/components/empty-state";

export const metadata: Metadata = { title: "Review" };

export default function ReviewPage() {
  return (
    <section className="flex flex-col gap-6">
      <h1 className="font-display text-headline text-ink">Review</h1>
      <EmptyState line="Words you save come back here, with the line you heard them in." action={{ label: "Find a song", href: "/library" }} />
    </section>
  );
}
