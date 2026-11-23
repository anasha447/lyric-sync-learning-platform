import { queryOptions } from "@tanstack/react-query";
import { ApiError, getJson } from "./api";
import { songKeys } from "./query-client";

export type Level = "A1" | "A2" | "B1" | "B2";

/** One timed line (GLOSSARY: Line), as GET /api/songs/{slug} returns it. */
export type Line = {
  index: number;
  text: string;
  startMs: number;
  endMs: number;
  instrumental: boolean;
  /** GLOSSARY: Line translation. Not every line has one yet. */
  translation: string | null;
};

export type PublicSong = {
  slug: string;
  title: string;
  artist: string;
  level: Level;
  durationMs: number;
  audioUrl: string;
  lines: Line[];
};

/** Published songs never go stale in a session, so going back to a song costs no request (ADR-0011). */
export const songQuery = (slug: string) =>
  queryOptions({
    queryKey: songKeys.detail(slug),
    queryFn: ({ signal }) => getJson<PublicSong>(`/api/songs/${encodeURIComponent(slug)}`, { signal }),
    // a 404 won't fix itself; a network or server failure might
    retry: (failures, error) => !(error instanceof ApiError && error.status < 500) && failures < 2,
  });
