import type { Metadata } from "next";
import Link from "next/link";
import { AccountBlock } from "@/components/shell/account-block";

export const metadata: Metadata = { title: "Profile" };

/** On phones the account block and Vocabulary live here (DESIGN.md → Layout → App shell). */
export default function ProfilePage() {
  return (
    <section className="flex flex-col gap-6">
      <h1 className="font-display text-headline text-ink">Profile</h1>
      <div className="rounded-lg border border-hairline bg-dusk p-6">
        <AccountBlock />
      </div>
      <Link href="/vocabulary" className="inline-flex min-h-11 items-center text-label text-violet-light hover:text-ink">
        Vocabulary
      </Link>
    </section>
  );
}
