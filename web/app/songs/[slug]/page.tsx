import type { Metadata } from "next";
import { SongScreen } from "@/components/player/song-screen";

type Props = { params: Promise<{ slug: string }> };

export const metadata: Metadata = { title: "Song" };

export default async function SongPage({ params }: Props) {
  const { slug } = await params;
  return <SongScreen slug={slug} />;
}
