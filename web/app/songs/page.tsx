import { redirect } from "next/navigation";

/** /songs/{slug} is a song; the list of songs lives in the Library. */
export default function SongsPage() {
  redirect("/library");
}
