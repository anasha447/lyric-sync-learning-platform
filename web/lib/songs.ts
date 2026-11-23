import { infiniteQueryOptions, queryOptions } from "@tanstack/react-query";
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

export const LEVELS: readonly Level[] = ["A1", "A2", "B1", "B2"];

/** One song in the library, as GET /api/songs lists it. */
export type SongSummary = {
  slug: string;
  title: string;
  artist: string;
  level: Level | null;
  durationMs: number;
};

export type SongPage = {
  items: SongSummary[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
};

// a 404 won't fix itself; a network or server failure might
const retryUnlessClientError = (failures: number, error: Error) =>
  !(error instanceof ApiError && error.status < 500) && failures < 2;

/** Published songs never go stale in a session, so going back to a song costs no request (ADR-0011). */
export const songQuery = (slug: string) =>
  queryOptions({
    queryKey: songKeys.detail(slug),
    queryFn: ({ signal }) => getJson<PublicSong>(`/api/songs/${encodeURIComponent(slug)}`, { signal }),
    retry: retryUnlessClientError,
  });

/** The library, a page at a time ("Show more songs"), optionally for one level. */
export const songListQuery = (level: Level | undefined) =>
  infiniteQueryOptions({
    queryKey: songKeys.list(level),
    queryFn: ({ pageParam, signal }) => {
      const params = new URLSearchParams({ page: String(pageParam) });
      if (level) params.set("level", level);
      return getJson<SongPage>(`/api/songs?${params}`, { signal });
    },
    initialPageParam: 0,
    getNextPageParam: (last) => (last.page + 1 < last.totalPages ? last.page + 1 : undefined),
    retry: retryUnlessClientError,
  });
