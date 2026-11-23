"use client";

import { AudioLines } from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import { isActive, sidebarItems } from "./nav";
import { AccountBlock } from "./account-block";

/** Tablet: 72 px, icons only. Desktop: 264 px with labels and the account block. */
export function Sidebar() {
  const pathname = usePathname();
  return (
    <aside
      className="fixed inset-y-0 left-0 z-30 hidden w-(--width-sidebar-collapsed) flex-col border-r border-hairline bg-dusk md:flex lg:w-(--width-sidebar)"
    >
      <Link
        href="/"
        className="flex h-16 items-center justify-center gap-2 px-6 font-display text-title text-ink lg:justify-start"
      >
        <AudioLines size={22} strokeWidth={1.75} className="text-violet-light" aria-hidden />
        <span className="sr-only lg:not-sr-only">lyricsync</span>
      </Link>

      <nav aria-label="Main" className="flex flex-1 flex-col gap-1 px-3 py-2">
        {sidebarItems.map(({ href, label, icon: Icon }) => {
          const active = isActive(pathname, href);
          return (
            <Link
              key={href}
              href={href}
              aria-current={active ? "page" : undefined}
              title={label}
              className={cn(
                "relative flex min-h-11 items-center justify-center gap-3 rounded-md px-3 text-label transition-colors duration-(--duration-instant) lg:justify-start",
                active ? "text-violet-light" : "text-ink-muted hover:bg-plum/50 hover:text-ink",
              )}
            >
              {active && (
                <span aria-hidden className="absolute inset-y-2 left-0 w-[3px] rounded-pill bg-violet" />
              )}
              <Icon size={20} strokeWidth={1.5} aria-hidden />
              <span className="sr-only lg:not-sr-only">{label}</span>
            </Link>
          );
        })}
      </nav>

      <div className="hidden border-t border-hairline p-4 lg:block">
        <AccountBlock />
      </div>
    </aside>
  );
}
