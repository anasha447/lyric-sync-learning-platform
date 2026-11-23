"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { cn } from "@/lib/utils";
import { isActive, tabItems } from "./nav";

/** Phones only: 64 px bar on blurred dusk, so content scrolling underneath stays faintly visible. */
export function TabBar() {
  const pathname = usePathname();
  return (
    <nav
      aria-label="Main"
      className="fixed inset-x-0 bottom-0 z-30 flex h-(--height-tab-bar) border-t border-hairline bg-dusk/82 pb-[env(safe-area-inset-bottom)] backdrop-blur-[20px] md:hidden"
    >
      {tabItems.map(({ href, label, icon: Icon }) => {
        const active = isActive(pathname, href);
        return (
          <Link
            key={href}
            href={href}
            aria-current={active ? "page" : undefined}
            className={cn(
              "flex min-h-11 flex-1 flex-col items-center justify-center gap-1 text-label transition-colors duration-(--duration-instant)",
              active ? "text-violet-light" : "text-ink-muted",
            )}
          >
            <Icon size={20} strokeWidth={1.5} aria-hidden />
            {label}
          </Link>
        );
      })}
    </nav>
  );
}
