import { BookOpen, House, Layers, Library, User, type LucideIcon } from "lucide-react";

export type NavItem = { href: string; label: string; icon: LucideIcon };

/** Sidebar on tablet and desktop (DESIGN.md → Layout → App shell). */
export const sidebarItems: NavItem[] = [
  { href: "/", label: "Home", icon: House },
  { href: "/library", label: "Library", icon: Library },
  { href: "/review", label: "Review", icon: Layers },
  { href: "/vocabulary", label: "Vocabulary", icon: BookOpen },
];

/** Bottom tab bar on phones: Vocabulary moves into Profile, next to the account block. */
export const tabItems: NavItem[] = [
  { href: "/", label: "Home", icon: House },
  { href: "/library", label: "Library", icon: Library },
  { href: "/review", label: "Review", icon: Layers },
  { href: "/profile", label: "Profile", icon: User },
];

/** "/" matches only Home; other items also match their sub-pages (/library/a2). */
export function isActive(pathname: string, href: string): boolean {
  if (href === "/") return pathname === "/";
  return pathname === href || pathname.startsWith(`${href}/`);
}
