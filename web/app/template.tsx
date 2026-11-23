import type { ReactNode } from "react";
import { PageTransition } from "@/components/shell/page-transition";

/** A template re-mounts on every navigation, so each page plays its entrance. */
export default function Template({ children }: { children: ReactNode }) {
  return <PageTransition>{children}</PageTransition>;
}
