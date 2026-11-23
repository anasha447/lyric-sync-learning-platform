import type { Metadata } from "next";
import { SongLibrary } from "@/components/songs/song-library";

export const metadata: Metadata = { title: "Library" };

export default function LibraryPage() {
  return (
    <section className="flex flex-col gap-6">
      <h1 className="font-display text-headline text-ink">Library</h1>
      <SongLibrary />
    </section>
  );
}
