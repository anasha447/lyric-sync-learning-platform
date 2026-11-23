"use client";

import { motion, useReducedMotion } from "motion/react";
import type { ReactNode } from "react";
import { pageTransition } from "@/lib/motion";

/** Pages fade in and rise 8 px; with reduced motion they only crossfade (DESIGN.md → Motion). */
export function PageTransition({ children }: { children: ReactNode }) {
  const reduce = useReducedMotion() ?? false;
  const t = pageTransition(reduce);
  return (
    <motion.div initial={t.initial} animate={t.animate}>
      {children}
    </motion.div>
  );
}
